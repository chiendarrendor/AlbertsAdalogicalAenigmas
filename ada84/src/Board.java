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
import grid.puzzlebits.Direction;

import java.awt.Point;

public class Board implements StandardFlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Deep CellContainer<CellType> cells;
    @Shallow int unknowns;
    @Shallow Point start = null;
    @Shallow Point end = null;

    public Board(String fname) {
        gfr = new GridFileReader(fname);

        LambdaInteger liunk = new LambdaInteger(0);
        cells = new CellContainer<CellType>(getWidth(),getHeight(),
                (x,y)-> {
                    if (hasClue(x,y)) {
                        return CellType.PATH;
                    } else {
                        liunk.inc();
                        return CellType.UNKNOWN;
                    }
                }
        );
        unknowns = liunk.get();

        for (int x = 0 ; x < getWidth() ; ++x ){
            for (int y = 0 ; y < getHeight() ; ++y) {
                if (!isTerminal(x,y)) continue;
                if (start == null) {
                    start = new Point(x,y);
                    continue;
                }
                if (end == null) {
                    end = new Point(x,y);
                    continue;
                }
                throw new RuntimeException("Too many Terminals");
            }
        }
        if (start == null || end == null) throw new RuntimeException("Not Enough Terminals");
    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public boolean inBounds(int x, int y) { return gfr.inBounds(x,y); }
    public boolean inBounds(Point p) { return gfr.inBounds(p); }
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.'; }
    public boolean hasClue(int x,int y) { return gfr.getBlock("DEADENDS")[x][y].charAt(0) != '.'; }
    public int getClue(int x,int y) { return Integer.parseInt( gfr.getBlock("DEADENDS")[x][y]); }
    public boolean isTerminal(int x,int y) { return gfr.getBlock("TERMINALS")[x][y].charAt(0) == '@'; }
    public String getSolution() { return gfr.getVar("SOLUTION"); }
    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }
    public CellType getCell(int x,int y) { return cells.getCell(x,y); }
    public void setCell(int x,int y,CellType ct) {
        cells.setCell(x,y,ct);
        --unknowns;
    }
    public Point getStart() { return start; }
    public Point getEnd() { return end; }

    public int getAdjacentPaths(int x,int y) {
        int result = 0;
        for (Direction d : Direction.orthogonals()) {
            Point op = d.delta(x,y,1);
            if (!inBounds(op.x,op.y)) continue;
            if (getCell(op.x,op.y) == CellType.PATH) ++result;
        }
        return result;
    }

    public static class MyMove {
        int x;
        int y;
        CellType ct;
        public MyMove(int x,int y,CellType ct) { this.x = x; this.y = y; this.ct = ct; }
        public boolean applyMove(Board b) {
            if (b.getCell(x,y) != CellType.UNKNOWN) return b.getCell(x,y) == ct;
            b.setCell(x,y,ct);
            return true;
        }
    }


    @Override public boolean isComplete() { return unknowns == 0; }
    @Override public boolean applyMove(Object o) { return ((MyMove)o).applyMove(this); }

    @Override public FlattenSolvableTuple<Board> getOneTuple(int x, int y) {
        if (getCell(x,y) != CellType.UNKNOWN) return null;
        Board b1 = new Board(this);
        Board b2 = new Board(this);
        MyMove mm1 = new MyMove(x,y,CellType.PATH);
        MyMove mm2 = new MyMove(x,y,CellType.WALL);
        mm1.applyMove(b1);
        mm2.applyMove(b2);
        return new FlattenSolvableTuple<Board>(b1,mm1,b2,mm2);
    }



}
