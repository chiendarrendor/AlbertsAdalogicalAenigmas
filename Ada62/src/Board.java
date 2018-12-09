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

public class Board implements StandardFlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Shallow int unknowns;
    @Deep CellContainer<CellState> cells;


    public Board(String arg) {
        gfr = new GridFileReader(arg);

        LambdaInteger unk = new LambdaInteger(0);
        cells = new CellContainer<CellState>(getWidth(),getHeight(),
                (x,y)-> {
                    if (isDot(x,y)) {
                        return CellState.WHITE;
                    } else {
                        unk.inc();
                        return CellState.UNKNOWN;
                    }
                } );
        unknowns = unk.get();

    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public boolean onBoard(int x,int y) { return gfr.inBounds(x,y); }
    public boolean onBoard(Point p) { return gfr.inBounds(p); }

    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.'; }
    private char getRawDot(int x,int y) { return gfr.getBlock("DOTS")[x][y].charAt(0);}
    public boolean isDot(int x,int y) { return getRawDot(x,y) != '.'; }
    public boolean isNumber(int x,int y) { return Character.isDigit(getRawDot(x,y)); }
    public int getNumber(int x,int y) { return Integer.parseInt(gfr.getBlock("DOTS")[x][y]); }

    public CellState getCell(int x,int y) { return cells.getCell(x,y); }
    public void setCell(int x,int y,CellState cs) {
        unknowns--;
        cells.setCell(x,y,cs);
    }

    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }


    public boolean isComplete() {
        return unknowns == 0;
    }

    public static class MyMove {
        int x;
        int y;
        CellState cs;
        public MyMove(int x,int y,CellState cs) { this.x = x; this.y = y; this.cs = cs; }
        public boolean applyMove(Board b) {
            if (b.getCell(x,y) != CellState.UNKNOWN) return b.getCell(x,y) == cs;
            b.setCell(x,y,cs);
            return true;
        }
    }

    public boolean applyMove(Object o) { return ((MyMove) o).applyMove(this); }

    @Override public FlattenSolvableTuple<Board> getOneTuple(int x, int y) {
        if (getCell(x,y) != CellState.UNKNOWN) return null;
        Board b1 = new Board(this);
        Board b2 = new Board( this);
        MyMove mm1 = new MyMove(x,y,CellState.BLACK);
        MyMove mm2 = new MyMove(x,y,CellState.WHITE);

        mm1.applyMove(b1);
        mm2.applyMove(b2);

        return new FlattenSolvableTuple<Board>(b1,mm1,b2,mm2);
    }


}
