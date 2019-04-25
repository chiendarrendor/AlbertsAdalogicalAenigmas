import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.lambda.CellLambda;
import grid.lambda.LambdaInteger;
import grid.lambda.XYLambda;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.StandardFlattenSolvable;
import grid.puzzlebits.CellContainer;

import java.awt.Point;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Board implements StandardFlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Deep CellContainer<CellType> cells;
    @Shallow int unknowns;
    @Shallow List<Point> pathHints = new ArrayList<>();

    public Board(String fname) {
        gfr = new GridFileReader(fname);

        if (gfr.hasVar("PATHHINTS")) {
            String[] hintitems = gfr.getVar("PATHHINTS").split(" ");
            if (hintitems.length % 2 != 0) throw new RuntimeException("PATHHINTS must have even number of numbers!");
            for (int i = 0 ; i < hintitems.length; i += 2) {
                int x = Integer.parseInt(hintitems[i]);
                int y = Integer.parseInt(hintitems[i+1]);
                if (!onBoard(x,y)) throw new RuntimeException("PATHHINTS x,y must be on board: " + x + " " + y);
                pathHints.add(new Point(x,y));
            }
        }

        LambdaInteger unk = new LambdaInteger(0);

        cells = new CellContainer<CellType>(getWidth(),getHeight(),(x,y)->{
           if (!isVista(x,y)) {
               unk.inc();
               return CellType.UNKNOWN;
           }
           return CellType.PATH;
        });

        unknowns = unk.get();


    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }


    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public boolean isVista(int x,int y) { return gfr.getBlock("NUMBERS")[x][y].charAt(0) != '.'; }
    public int vistaNumber(int x,int y) { return Integer.parseInt(gfr.getBlock("NUMBERS")[x][y]); }
    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.'; }
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }
    public boolean onBoard(int x,int y) { return gfr.inBounds(x,y); }
    public boolean onBoard(Point p) { return gfr.inBounds(p); }

    public CellType getCell(int x,int y) { return cells.getCell(x,y); }
    public void setCell(int x,int y,CellType ct) { cells.setCell(x,y,ct); unknowns--; }

    public void fillFrom(String outfile) {
        GridFileReader filler = new GridFileReader(outfile);
        forEachCell((x,y)->{
           char c = filler.getBlock("RESULT")[x][y].charAt(0);

           switch(c) {
               case '.':
                   if (!isVista(x,y)) throw new RuntimeException("load-file mismatch against vistas!");
                   return;
               case 'P':
                    setCell(x,y,CellType.PATH);
                    break;
               case 'T':
                    setCell(x,y,CellType.TREE);
                    break;
               default:
                   throw new RuntimeException("unknown character in load-file");
           }
        });
    }

    public void writeTo(String outfile) {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(outfile));
            writer.println("" + getWidth() + " " + getHeight());
            writer.println("CODE:RESULT");
            forEachCell((x,y)-> {
                if (isVista(x,y)) writer.print(".");
                else {
                    switch(getCell(x,y)) {
                        case UNKNOWN: writer.print("U"); break;
                        case TREE: writer.print("T"); break;
                        case PATH: writer.print("P"); break;
                    }
                }


                if (x == getWidth() - 1) writer.println("");
            });

            writer.close();

        } catch (IOException e) {
            throw new RuntimeException("Can't write.",e);
        }
    }

    private static class MyMove {
        int x;
        int y;
        CellType ct;
        public MyMove(int x,int y,CellType ct) { this.x = x; this.y = y; this.ct = ct; }
        public boolean applyMove(Board b) {
            CellType curct = b.getCell(x,y);
            if (curct != CellType.UNKNOWN) return ct == curct;
            b.setCell(x,y,ct);
            return true;
        }
    }

    public boolean isComplete() { return unknowns == 0; }
    public boolean applyMove(Object o) { return ((MyMove)o).applyMove(this); }
    public FlattenSolvableTuple<Board> getOneTuple(int x, int y) {
        if (this.getCell(x,y) != CellType.UNKNOWN) return null;
        Board b1 = new Board(this);
        Board b2 = new Board(this);
        MyMove mm1 = new MyMove(x,y,CellType.TREE);
        MyMove mm2 = new MyMove(x,y,CellType.PATH);
        mm1.applyMove(b1);
        mm2.applyMove(b2);
        return new FlattenSolvableTuple<Board>(b1,mm1,b2,mm2);
    }
}
