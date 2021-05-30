import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.lambda.CellLambda;
import grid.lambda.XYLambda;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.StandardFlattenSolvable;
import grid.puzzlebits.CellContainer;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Board implements StandardFlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Shallow List<Block> blocks;
    @Deep CellContainer<CellState> cells;

    public Board(String fname) {
        gfr = new GridFileReader(fname);
        int blockcount = Integer.parseInt(gfr.getVar("BLOCKCOUNT"));
        int blocksize = Integer.parseInt(gfr.getVar("MAXNUM"));
        blocks = new ArrayList<>();

        for (int i = 1 ; i <= blockcount; ++i ) {
            String vname = "BLOCK" + i;
            String[] parts = gfr.getVar(vname).split(" ");
            blocks.add(new Block(Integer.parseInt(parts[0]),Integer.parseInt(parts[1]),blocksize));
        }

        cells = new CellContainer<CellState>(getWidth(),getHeight(),
                (x,y)-> hasLetter(x,y) ? new CellState(blocksize) : null,
                (x,y,r) -> r == null ? null : new CellState(r)
        );
    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public boolean hasRowClue(Point p) { return !gfr.getBlock("LEFTRIGHTCLUES")[p.x][p.y].equals("."); };
    public boolean hasColumnClue(Point p) { return !gfr.getBlock("UPDOWNCLUES")[p.x][p.y].equals("."); }
    public int getRowClue(Point p) { return Integer.parseInt(gfr.getBlock("LEFTRIGHTCLUES")[p.x][p.y]);}
    public int getColumnClue(Point p) { return Integer.parseInt(gfr.getBlock("UPDOWNCLUES")[p.x][p.y]); }
    public boolean hasLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0) != '*'; }
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }


    public CellState getCell(int x,int y) { return cells.getCell(x,y); }

    public interface ByBlock { public void go(Block b); }
    public void forEachBlock(ByBlock bb) {
        for(Block b : blocks) bb.go(b);
    }


    @Override public boolean isComplete() {
        for (int y = 0 ; y < getHeight() ; ++y) {
            for (int x = 0 ; x < getWidth() ; ++x) {
                CellState cs = getCell(x,y);
                if (cs == null) continue;
                if (!cs.complete()) return false;
            }
        }
        return true;
    }


    private static class MyMove {
        int x;
        int y;
        int v;
        boolean doSet;
        public MyMove(int x,int y,int v,boolean doSet) { this.x = x; this.y = y; this.v = v; this.doSet = doSet; }

        public boolean applyMove(Board b) {
            CellState cs = b.getCell(x,y);
            if (doSet) {
                if (!cs.contains(v)) return false;
                cs.set(v);
                return true;
            } else {
                cs.remove(v);
                return true;
            }
        }
    }



    @Override public boolean applyMove(Object o) {  return ((MyMove)o).applyMove(this); }

    @Override public FlattenSolvableTuple<Board> getOneTuple(int x, int y) {
        CellState cs = getCell(x,y);
        if (cs == null) return null;
        if (cs.complete() || cs.broken()) return null;
        Collection<Integer> possibles = cs.getSet();
        FlattenSolvableTuple<Board> fst = new FlattenSolvableTuple<>();
        for (int possible : possibles) {
            MyMove pro = new MyMove(x,y,possible,true);
            MyMove anti = new MyMove(x,y,possible,false);
            Board b = new Board(this);
            pro.applyMove(b);
            fst.addTuple(b,anti);
        }

        return fst;
    }

}
