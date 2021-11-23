import grid.copycon.CopyCon;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.MultiFlattenSolvable;
import grid.puzzlebits.Direction;
import grid.puzzlebits.PointAdjacency;
import grid.solverrecipes.singleloopflatten.EdgeState;
import grid.solverrecipes.singleloopflatten.SingleLoopBoard;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Board extends SingleLoopBoard<Board> implements MultiFlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Shallow Map<Character,Set<Point>> cellsByRegion;
    @Shallow Map<Character,Integer> regionSize;
    public Board(String fname) {
        gfr = new GridFileReader(fname);
        init();

        cellsByRegion = new HashMap<>();
        regionSize = new HashMap<>();

        forEachCell((x,y)-> {
            char rid = getRegion(x,y);
            if (!cellsByRegion.containsKey(rid)) cellsByRegion.put(rid,new HashSet<>());
            cellsByRegion.get(rid).add(new Point(x,y));
            if (hasNumber(x,y)) {
                if (regionSize.containsKey(rid)) throw new RuntimeException("Region " + rid + " contains multiple numbers");
                regionSize.put(rid,getNumber(x,y));
            }

            if (hasLetter(x,y) && hasNumber(x,y)) throw new RuntimeException("Cell " + x + "," + y + " has letter and number");
            if (!hasLetter(x,y) && !hasNumber(x,y)) throw new RuntimeException("Cell " + x + "," + y + " has neither letter nor number");
        });

        for (Map.Entry<Character,Set<Point>> regionentry : cellsByRegion.entrySet()) {
            if (!PointAdjacency.allAdjacent(regionentry.getValue(),false))
                throw new RuntimeException("Region " + regionentry.getKey() + " contains non-adjacent cells");
        }



    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }

    @Override public int getWidth() { return gfr.getWidth(); }
    @Override public int getHeight() { return gfr.getHeight(); }
    public String getTitle() { return gfr.getVar("TITLE"); }
    public String getSolution() { return gfr.getVar("SOLUTION"); }
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.'; }
    public boolean hasNumber(int x,int y) { return !gfr.getBlock("NUMBERS")[x][y].equals("."); }
    public int getNumber(int x,int y) { return Integer.parseInt(gfr.getBlock("NUMBERS")[x][y]); }
    public char getRegion(int x,int y) { return gfr.getBlock("REGIONS")[x][y].charAt(0); }
    public boolean inBounds(Point p) { return gfr.inBounds(p); }

    public Collection<Character> getRegionIds() { return cellsByRegion.keySet(); }
    public Set<Point> getRegionCells(char rid){ return cellsByRegion.get(rid); }
    public boolean hasRegionSize(char rid) { return regionSize.containsKey(rid); }
    public int getRegionSize(char rid) { return regionSize.get(rid); }



    @Override public boolean isComplete() {
        return getUnknownCount() == 0;
    }

    private static class MyMove {
        int x;
        int y;
        Direction d;
        EdgeState es;
        public MyMove(int x,int y,Direction d, EdgeState es) { this.x = x; this.y = y; this.d = d; this.es = es; }
        public boolean applyMove(Board b) {
            if (b.getEdge(x,y,d) != EdgeState.UNKNOWN) return b.getEdge(x,y,d) == es;
            b.setEdge(x,y,d,es);
            return true;
        }
    }


    @Override public boolean applyMove(Object o) { return ((MyMove)o).applyMove(this); }

    private void getTupleForCellAndDirection(int x,int y,Direction d,List<FlattenSolvableTuple<Board>> result) {
        if (getEdge(x,y,d) != EdgeState.UNKNOWN) return;
        Board b1 = new Board(this);
        Board b2 = new Board(this);
        MyMove mm1 = new MyMove(x,y,d,EdgeState.PATH);
        MyMove mm2 = new MyMove(x,y,d,EdgeState.WALL);
        mm1.applyMove(b1);
        mm2.applyMove(b2);
        result.add(new FlattenSolvableTuple<Board>(b1,mm1,b2,mm2));
    }


    @Override public List<FlattenSolvableTuple<Board>> getTuplesForCell(int x, int y) {
        List<FlattenSolvableTuple<Board>> result = new ArrayList<>();
        getTupleForCellAndDirection(x,y,Direction.EAST,result);
        getTupleForCellAndDirection(x,y,Direction.SOUTH,result);
        return result;
    }

}
