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
    @Deep CellContainer<CellState> cells;
    @Shallow int unknowns;

    public Board(String fname) {
        gfr = new GridFileReader(fname);

        LambdaInteger unk = new LambdaInteger(0);
        cells = new CellContainer<CellState>(getWidth(),getHeight(),
                (x,y) -> {
                    if (hasNumber(x,y)) return CellState.NUMBER;
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
    public String getSolution() { return gfr.getVar("SOLUTION"); }

    public boolean inBounds(int x,int y) { return gfr.inBounds(x,y); }
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.'; }
    public boolean hasNumber(int x,int y) { return gfr.getBlock("CLUES")[x][y].charAt(0) != '.'; }
    public int getNumber(int x,int y) { return Integer.parseInt(gfr.getBlock("CLUES")[x][y]); }
    public CellState getCell(int x,int y) { return cells.getCell(x,y); }
    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }

    public void setCell(int x,int y,CellState cs) {
        if (cs == CellState.NUMBER) throw new RuntimeException("Should never set to NUMBER");
        CellState orig = getCell(x,y);

        if (orig == CellState.UNKNOWN && cs != CellState.UNKNOWN) --unknowns;
        if (orig != CellState.UNKNOWN && cs == CellState.UNKNOWN) ++unknowns;
        cells.setCell(x,y,cs);
    }

    private static class MyMove {
        int x;
        int y;
        CellState cs;
        public MyMove(int x,int y,CellState cs) { this.x = x; this.y = y; this.cs = cs; }
        public boolean applyMove(Board thing) {
            if (thing.getCell(x,y) != CellState.UNKNOWN) return thing.getCell(x,y) == cs;
            thing.setCell(x,y,cs);
            return true;
        }
        public Board child(Board parent) { Board nb = new Board(parent); applyMove(nb); return nb; }
    }

    @Override public boolean isComplete() { return unknowns == 0; }
    @Override public boolean applyMove(Object o) { return ((MyMove)o).applyMove(this); }

    @Override public FlattenSolvableTuple<Board> getOneTuple(int x, int y) {
        if (getCell(x,y) != CellState.UNKNOWN) return null;
        MyMove mm1 = new MyMove(x,y,CellState.HORIZONTAL);
        MyMove mm2 = new MyMove(x,y,CellState.VERTICAL);
        return new FlattenSolvableTuple<Board>(mm1.child(this),mm1,mm2.child(this),mm2);
    }


}
