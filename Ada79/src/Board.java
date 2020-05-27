import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.graph.GridGraph;
import grid.lambda.CellLambda;
import grid.lambda.XYLambda;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.StandardFlattenSolvable;
import grid.puzzlebits.CellContainer;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Board implements StandardFlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Deep CellContainer<CellState> cells;
    @Shallow int unknowns;
    @Shallow Map<Character,List<Point>> regions = new HashMap<>();
    @Shallow Map<Character,Integer> regionsize = new HashMap<>();

    private class RegionGGR implements GridGraph.GridReference {
        char region;
        public RegionGGR(char region) { this.region = region; }

        @Override public int getWidth() { return Board.this.getWidth(); }
        @Override public int getHeight() { return Board.this.getHeight(); }
        @Override public boolean isIncludedCell(int x, int y) { return getRegionId(x,y) == region; }
        @Override public boolean edgeExitsEast(int x, int y) { return true; }
        @Override public boolean edgeExitsSouth(int x, int y) { return true; }
    }


    public Board(String fname) {
        gfr = new GridFileReader(fname);
        cells = new CellContainer<CellState> (getWidth(),getHeight(),(x,y)->CellState.UNKNOWN);
        unknowns = getWidth() * getHeight();

        forEachCell((x,y)-> {
            char rid = getRegionId(x,y);
            if (!regions.containsKey(rid)) regions.put(rid,new ArrayList<>());
            if (hasClue(x,y)) {
                if (regionsize.containsKey(rid)) throw new RuntimeException("Found more than one clue for region (" + rid + ")");
                regionsize.put(rid,Integer.parseInt(gfr.getBlock("CLUES")[x][y]));
            }
            regions.get(rid).add(new Point(x,y));
        });

        for (char region : regions.keySet()) {
            GridGraph gg = new GridGraph(new RegionGGR(region));
            if (!gg.isConnected()) throw new RuntimeException("Region (" + region + ") is not connected!");
        }


    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }
    public boolean hasClue(int x,int y) { return !gfr.getBlock("CLUES")[x][y].equals("."); }
    public int getClue(int x,int y) { return Integer.parseInt(gfr.getBlock("CLUES")[x][y]); }
    public char getRegionId(int x,int y) { return gfr.getBlock("REGIONS")[x][y].charAt(0); }
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.'; }
    public boolean onBoard(int x,int y) { return gfr.inBounds(x,y); }

    public Set<Character> getCluedRegions() { return regionsize.keySet(); }
    public int getRegionClue(char c) { return regionsize.get(c); }
    public List<Point> getRegionCells(char c) { return regions.get(c); }
    public CellState getCell(int x,int y) { return cells.getCell(x,y); }
    public void setCell(int x, int y, CellState cs) { cells.setCell(x,y,cs); unknowns--; }


    @Override public boolean isComplete() { return unknowns == 0; }

    private static class MyMove {
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

    @Override public boolean applyMove(Object o) { return ((MyMove)o).applyMove(this); }

    @Override public FlattenSolvableTuple<Board> getOneTuple(int x, int y) {
        if (getCell(x,y) != CellState.UNKNOWN) return null;
        Board b1 = new Board(this);
        Board b2 = new Board(this);
        MyMove mm1 = new MyMove(x,y,CellState.SHADED);
        MyMove mm2 = new MyMove(x,y,CellState.UNSHADED);

        mm1.applyMove(b1);
        mm2.applyMove(b2);
        return new FlattenSolvableTuple<Board>(b1,mm1,b2,mm2);
    }



}
