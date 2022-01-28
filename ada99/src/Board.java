import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.StandardFlattenSolvable;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.Collection;
import java.util.Set;

public class Board implements StandardFlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Shallow MergingRegions regions;
    @Deep CellContainer<CellState> cells;
    @Shallow int unknownCount;

    public Board(String fname) {
        gfr = new GridFileReader(fname);
        cells = new CellContainer<CellState>(getWidth(),getHeight(),
                (x,y)->CellState.UNKNOWN);
        regions = new MergingRegions(getWidth(),getHeight(),gfr.getBlock("REGIONLINKS"));
        unknownCount = getWidth() * getHeight();
    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public boolean isQuadInBounds(int x, int y) { return gfr.inBounds(x+1,y+1); }
    public boolean inBounds(Point p) { return gfr.inBounds(p); }
    public int getRegionId(int x, int y) { return regions.getRegionId(x,y); }
    public Collection<Integer> getRegionIds() { return regions.getRegionIds(); }
    public Set<Point> getRegionCells(int rid) { return regions.getRegionPoints(rid); }
    public CellState getCell(int x, int y) { return cells.getCell(x,y); }
    public char getLetter(int x, int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.'; }
    public int getNumber(int x, int y) { return Integer.parseInt(gfr.getBlock("NUMBERS")[x][y]); }
    public boolean hasNumber(int x,int y) { return !".".equals(gfr.getBlock("NUMBERS")[x][y]); }
    public String getName() { return gfr.getVar("NAME"); }
    public String getSolution() { return gfr.getVar("SOLUTION"); }

    public int shadedCount(int x, int y) {
        int result = 0;
        for (Direction d: Direction.orthogonals()) {
            Point op = d.delta(x,y,1);
            if (!inBounds(op)) continue;
            if (getCell(op.x,op.y) != CellState.SHADED) continue;
            ++result;
        }
        return result;
    }



    public void setCell(int x,int y,CellState cs) {
        cells.setCell(x,y,cs);
        --unknownCount;
    }

    private static class MyMove {
        int x;
        int y;
        CellState cs;
        public MyMove(int x,int y,CellState cs) { this.x = x; this.y = y; this.cs = cs; }
        public boolean applyMove(Board b) {
            if (b.getCell(x,y) != CellState.UNKNOWN) return cs == b.getCell(x,y);
            b.setCell(x,y,cs);
            return true;
        }
    }


    @Override public boolean isComplete() { return unknownCount == 0; }
    @Override public boolean applyMove(Object o) { return ((MyMove)o).applyMove(this); }

    @Override public FlattenSolvableTuple<Board> getOneTuple(int x, int y) {
        if (getCell(x,y) != CellState.UNKNOWN) return null;
        Board b1 = new Board(this);
        Board b2 = new Board(this);
        MyMove mm1 = new MyMove(x,y,CellState.UNSHADED);
        MyMove mm2 = new MyMove(x,y,CellState.SHADED);
        mm1.applyMove(b1);
        mm2.applyMove(b2);
        return new FlattenSolvableTuple<Board>(b1,mm1,b2,mm2);
    }



}
