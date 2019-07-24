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

public class Board implements StandardFlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Shallow int unknowns;
    @Deep CellContainer<CellState> cells;

    public Board(String fname) {
        gfr = new GridFileReader(fname);

        LambdaInteger unk = new LambdaInteger(0);
        cells = new CellContainer<CellState>(getWidth(),getHeight(),(x,y)-> {
            unk.inc();
            return CellState.UNKNOWN;
        });

        unknowns = unk.get();

    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }

    public CellState getCell(int x,int y) { return cells.getCell(x,y); }
    public void setCell(int x,int y,CellState cs) {
        --unknowns;
        cells.setCell(x,y,cs);
    }

    public boolean hasNumber(int x,int y) { return !".".equals(gfr.getBlock("NUMBERS")[x][y]); }
    public int getNumber(int x,int y) { return Integer.parseInt(gfr.getBlock("NUMBERS")[x][y]); }
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.'; }
    public char getRegion(int x,int y) { return gfr.getBlock("REGIONS")[x][y].charAt(0); }
    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }

    @Override public boolean isComplete() { return unknowns == 0; }

    private static class MyMove {
        int x;
        int y;
        CellState cs;
        public MyMove(int x,int y,CellState cs) { this.x = x; this.y = y; this.cs = cs; }

        public boolean applyMove(Board b) {
            CellState curcs = b.getCell(x,y);
            if (curcs != CellState.UNKNOWN) return curcs == cs;
            b.setCell(x,y,cs);
            return true;
        }
    }

    @Override public boolean applyMove(Object o) { return ((MyMove)o).applyMove(this); }

    @Override public FlattenSolvableTuple<Board> getOneTuple(int x, int y) {
        if (getCell(x,y) != CellState.UNKNOWN) return null;
        Board b1 = new Board(this);
        Board b2 = new Board(this);
        MyMove mm1 = new MyMove(x,y,CellState.HORIZONTAL);
        MyMove mm2 = new MyMove(x,y,CellState.VERTICAL);
        mm1.applyMove(b1);
        mm2.applyMove(b2);
        return new FlattenSolvableTuple<Board>(b1,mm1,b2,mm2);
    }


}
