import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.lambda.CellLambda;
import grid.lambda.XYLambda;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.StandardFlattenSolvable;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;

public class Board implements StandardFlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Deep CellContainer<CellSet> cells;

    public Board(String fname) {
        gfr = new GridFileReader(fname);
        if (getWidth() != getHeight()) throw new RuntimeException("Must be Square");

        cells = new CellContainer<CellSet>(getWidth(),getHeight(),
                (x,y)->  new CellSet(getWidth()),
                (x,y,old)->old == null ? null : new CellSet(old)
        );


        forEachCell((x,y)-> {
            String istr = gfr.getBlock("INITIALS")[x][y];
            if (istr.equals(".")) return;
            int inum = Integer.parseInt(istr);
            getCellSet(x,y).is(inum);

        });
    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public boolean onBoard(int x,int y) { return gfr.inBounds(x,y); }
    public boolean isSpecial(int x,int y) { return gfr.getBlock("SPECIALS")[x][y].charAt(0) == 'o'; }
    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }
    public CellSet getCellSet(int x,int y) { return cells.getCell(x,y); }

    public boolean hasInequality(int x,int y) { return !gfr.getBlock("INEQUALITIES")[x][y].equals("."); }
    public Direction getInequalityDirection(int x,int y) { return Direction.fromShort(gfr.getBlock("INEQUALITIES")[x][y].substring(1)); }
    public char getInequalitySymbol(int x,int y) { return gfr.getBlock("INEQUALITIES")[x][y].charAt(0); }
    public boolean hasDiff(int x, int y) { return !gfr.getBlock("DIFFS")[x][y].equals("."); }
    public Direction getDiffDirection(int x,int y) { return Direction.fromShort(gfr.getBlock("DIFFS")[x][y].substring(1)); }
    public int getDiffSize(int x,int y) { return Integer.parseInt(gfr.getBlock("DIFFS")[x][y].substring(0,1)); }



    // FlattenSolvable
    @Override public boolean isComplete() {
        return CellLambda.stream(getWidth(),getHeight())
                .allMatch(p->getCellSet(p.x,p.y).size() == 1);
    }

    private static class MyMove {
        int x;
        int y;
        int num;
        boolean isNumber;
        public MyMove(int x,int y,int num,boolean isNumber) {this.x = x; this.y = y; this.num = num; this.isNumber = isNumber; }
        public boolean applyMove(Board b) {
            if (isNumber) return b.getCellSet(x,y).is(num);
            b.getCellSet(x,y).isNot(num);
            return true;
        }

    }


    @Override public boolean applyMove(Object o) { return ((MyMove)o).applyMove(this);  }

    @Override public FlattenSolvableTuple<Board> getOneTuple(int x, int y) {
        CellSet cs = getCellSet(x,y);
        if (cs.size() < 2) return null;
        FlattenSolvableTuple<Board> fst = new FlattenSolvableTuple<>();
        for (Integer i : cs) {
            Board b1 = new Board(this);
            Board b2 = new Board( this);
            MyMove mm1 = new MyMove(x,y,i,true);
            MyMove mm2 = new MyMove(x,y,i,false);
            mm1.applyMove(b1);
            mm2.applyMove(b2);

            fst.addTuple(b1,mm2);
            fst.addTuple(b2,mm1);
        }
        return fst;
    }

}
