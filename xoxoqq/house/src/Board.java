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

public class Board implements StandardFlattenSolvable<Board>{
    @Shallow GridFileReader gfr;
    @Deep CellContainer<CellType> cells;
    @Deep CellContainer<Integer> regions;
    @Shallow int unknowns;

    public Board(String fname) {
        gfr = new GridFileReader(fname);

        LambdaInteger unk = new LambdaInteger(0);

        cells = new CellContainer<CellType>(getWidth(),getHeight(),(x,y)->{
            if (!onBoard(x,y)) return CellType.BLACK;
            if (hasNumber(x,y)) return CellType.WHITE;

            unk.inc();
            return CellType.UNKNOWN;
        });

        LambdaInteger regid = new LambdaInteger(1);
        regions = new CellContainer<Integer>(getWidth(),getHeight(),(x,y)->{
            if (!hasNumber(x,y)) return null;
            int result = regid.get();
            regid.inc();
            return result;
        });




        unknowns = unk.get();
    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }

    public boolean onBoard(int x,int y) { return gfr.inBounds(x,y) && gfr.getBlock("CELLS")[x][y].charAt(0) == '.'; }
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.'; }
    public boolean hasNumber(int x,int y) { return gfr.getBlock("NUMBERS")[x][y].charAt(0) != '.'; }
    public int getNumber(int x,int y) { return Integer.parseInt(gfr.getBlock("NUMBERS")[x][y]); }

    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }

    public CellType getCell(int x,int y) { return cells.getCell(x,y); }
    public void setCell(int x,int y,CellType ct) { --unknowns; cells.setCell(x,y,ct);}
    public Integer getRegion(int x,int y) { return regions.getCell(x,y); }
    public void setRegion(int x,int y,int regid) { regions.setCell(x,y,regid); }


    @Override public boolean isComplete() { return unknowns == 0; }

    private static class MyMove {
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


    @Override public boolean applyMove(Object o) { return ((MyMove)o).applyMove(this); }

    @Override public FlattenSolvableTuple<Board> getOneTuple(int x, int y) {
        if (getCell(x,y) != CellType.UNKNOWN) return null;
        Board b1 = new Board(this);
        Board b2 = new Board(this);
        MyMove mm1 = new MyMove(x,y,CellType.BLACK);
        MyMove mm2 = new MyMove(x,y,CellType.WHITE);
        mm1.applyMove(b1);
        mm2.applyMove(b2);
        return new FlattenSolvableTuple<Board>(b1,mm1,b2,mm2);
    }


}
