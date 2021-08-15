import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.lambda.BooleanXYLambda;
import grid.lambda.CellLambda;
import grid.lambda.XYLambda;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.StandardFlattenSolvable;
import grid.puzzlebits.CellContainer;

import java.util.Collection;

public class Board implements StandardFlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Deep CellContainer<Cell> cells;
    @Deep PossibleFutons possibles;

    public Board(String fname) {
        gfr = new GridFileReader(fname);

        cells = new CellContainer<Cell>(getWidth(),getHeight(),
                (x,y)-> new Cell(hasPillar(x,y)),
                (x,y,r) -> new Cell(r)
        );

        possibles = new PossibleFutons(getWidth(),getHeight());
    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public boolean inBounds(int x,int y) { return gfr.inBounds(x,y); }
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.'; }
    public boolean hasPillar(int x,int y) { return gfr.getBlock("PILLARS")[x][y].charAt(0) != '.'; }
    public boolean hasNumericPillar(int x,int y) { return gfr.getBlock("PILLARS")[x][y].charAt(0) != '@';}
    public int getNumericPillarValue(int x,int y) { return Integer.parseInt(gfr.getBlock("PILLARS")[x][y]); }
    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }
    public boolean terminatingForEachCell(BooleanXYLambda bxyl) { return CellLambda.terminatingForEachCell(getWidth(),getHeight(),bxyl); }

    public Cell getCell(int x,int y) { return cells.getCell(x,y); }

    public FutonCell getFutonCell(int x,int y) { return possibles.getCell(x,y); }
    public Collection<FutonPair> getSetFutons() { return possibles.getSetFutons(); }
    public PossibleFutons getPossibles() { return possibles; }


    private static class MyMove {
        int x;
        int y;
        CellType ct;
        boolean doSet;

        public MyMove(int x,int y,CellType ct, boolean doSet) { this.x = x; this.y = y; this.ct = ct; this.doSet = doSet; }
        public boolean applyMove(Board b) {
            Cell c = b.getCell(x,y);
            if (doSet) {
                if (!c.has(ct)) return false;
                c.set(ct);
            } else {
                c.clear(ct);
            }
            return true;
        }
    }


    @Override public boolean applyMove(Object o) { return ((MyMove)o).applyMove(this); }
    @Override public boolean isComplete() { return terminatingForEachCell((x,y)->getCell(x,y).isDone()); }

    @Override public FlattenSolvableTuple<Board> getOneTuple(int x, int y) {
        Cell c = getCell(x,y);
        if (!c.isValid()) return null;
        if (c.isDone()) return null;
        FlattenSolvableTuple<Board> fst = new FlattenSolvableTuple<>();
        for (CellType ct : c.getPossibles()) {
            MyMove pro = new MyMove(x,y,ct,true);
            MyMove anti = new MyMove(x,y,ct,false);
            Board b = new Board(this);
            pro.applyMove(b);
            fst.addTuple(b,anti);
        }

        return fst;
    }


}
