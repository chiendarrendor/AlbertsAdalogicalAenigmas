import grid.copycon.CopyCon;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.logic.flatten.FlattenSolvable;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.puzzlebits.Direction;
import grid.puzzlebits.EdgeContainer;
import grid.solverrecipes.singleloopflatten.EdgeState;
import grid.solverrecipes.singleloopflatten.SingleLoopBoard;
import grid.spring.GridFrame;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Board extends SingleLoopBoard<Board> implements FlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Shallow Map<Character,RegionInfo> regionsById = new HashMap<>();
    @Shallow List<EdgeContainer.EdgeCoord> allEdges = new ArrayList<>();

    private RegionInfo gocRegion(char rid) {
        if (!regionsById.containsKey(rid)) regionsById.put(rid,new RegionInfo(rid));
        return regionsById.get(rid);
    }

    private void addSeparatingEdgePair(int x,int y,Direction d,RegionInfo myr) {
        Point otherp = d.delta(x,y,1);
        if (!inBounds(otherp)) return;
        char otherrid = getRegionId(otherp.x,otherp.y);
        if (myr.regionId == otherrid) return;
        RegionInfo otherr = gocRegion(otherrid);
        myr.addEdge(x,y,d);
        otherr.addEdge(otherp.x,otherp.y,d.getOpp());
    }




    public Board(String fname) {
        gfr = new GridFileReader(fname);
        init();

        for (int y = 0 ; y < getHeight() ; ++y) {
            for (int x = 0; x < getWidth() ; ++x) {
                RegionInfo myr = gocRegion(getRegionId(x,y));
                myr.addCell(x,y);

                addSeparatingEdgePair(x,y,Direction.EAST,myr);
                addSeparatingEdgePair(x,y,Direction.SOUTH,myr);
            }
        }

        for (char c : regionsById.keySet()) {
            regionsById.get(c).verifyConnectivity();
        }

        forEachEdge((x,y,isV,d)-> allEdges.add(new EdgeContainer.EdgeCoord(x,y,isV)));

    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }


    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public boolean inBounds(int x,int y) { return gfr.inBounds(x,y); }
    public boolean inBounds(Point p) { return gfr.inBounds(p); }
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.'; }
    public char getRegionId(int x,int y) { return gfr.getBlock("REGIONS")[x][y].charAt(0); }
    public Collection<Character> getRegionIds() { return regionsById.keySet(); }
    public RegionInfo getRegionInfo(char rid) { return regionsById.get(rid); }

    @Override public boolean isComplete() { return getUnknownCount() == 0; }


    private static class MyMove {
        int x;
        int y;
        boolean isV;
        EdgeState es;

        public MyMove(int x,int y,boolean isV,EdgeState es) {
            this.x = x; this.y = y; this.isV = isV;
            this.es = es;
        }

        public boolean applyMove(Board b) {
            EdgeState cures = b.getEdge(x,y,isV);
            if (cures != EdgeState.UNKNOWN) return cures == es;
            b.setEdge(x,y,isV,es);
            return true;
        }

    }

    @Override public boolean applyMove(Object o) { return ((MyMove)o).applyMove(this); }

    private FlattenSolvableTuple<Board> getTupleForEdge(int x,int y,boolean isV) {
        Board b1 = new Board(this);
        Board b2 = new Board(this);
        MyMove mm1 = new MyMove(x,y,isV, EdgeState.PATH);
        MyMove mm2 = new MyMove(x,y,isV,EdgeState.WALL);
        mm1.applyMove(b1);
        mm2.applyMove(b2);
        return new FlattenSolvableTuple<Board>(b1,mm1,b2,mm2);
    }

    private List<FlattenSolvableTuple<Board>> getEdgeTuples(boolean onlyOne) {
        List<FlattenSolvableTuple<Board>> result = new ArrayList<>();
        for(EdgeContainer.EdgeCoord ec : orderEdges()) {
            FlattenSolvableTuple<Board> fst = getTupleForEdge(ec.x,ec.y,ec.isV);
            result.add(fst);
            if (onlyOne) break;
        }
        return result;
    }


    private class RegionPair implements Comparable<RegionPair> {
        RegionInfo ri;
        RegionInfo.CountInfo ci;
        int grade;

        public RegionPair(RegionInfo ri) {
            this.ri = ri;
            this.ci = ri.getCounts(Board.this);
            grade = Math.min(4-this.ci.getPathCount(),this.ci.getPathCount()+this.ci.getUnknowns().size());
        }

        @Override public int compareTo(RegionPair o) {
            return Integer.compare(grade,o.grade);
        }
    }

    // given the state of the board, return a list of all UNKNOWN edges
    // sorted by how close the region is to complete
    // (minimum value of (4-paths),(paths+unknowns))
    //
    // note: we need to have a list of all edges, not just those that
    // cross regions, so we'll add those in after.
    public List<EdgeContainer.EdgeCoord> orderEdges() {
        List<RegionPair> pairs =
            getRegionIds().stream().map(rid->new RegionPair(getRegionInfo(rid)))
                .sorted().collect(Collectors.toList());


        List<EdgeContainer.EdgeCoord> result = new ArrayList<>();
        Set<EdgeContainer.EdgeCoord> found = new HashSet<>();
        Set<EdgeContainer.EdgeCoord> remaining = new HashSet<>();
        remaining.addAll(allEdges);

        for (RegionPair rp : pairs) {
            for (EdgeContainer.CellCoord cc : rp.ri.edges) {
                EdgeState es = getEdge(cc.x,cc.y,cc.d);
                if (es != EdgeState.UNKNOWN) {
                    continue;
                }
                EdgeContainer.EdgeCoord ec = new EdgeContainer.EdgeCoord(cc);
                if (found.contains(ec)) {
                    continue;
                }
                remaining.remove(ec);
                found.add(ec);
                result.add(ec);
            }
        }

        for (EdgeContainer.EdgeCoord ec : remaining) {
            EdgeState es = getEdge(ec.x,ec.y,ec.isV);
            if (es != EdgeState.UNKNOWN) continue;
            result.add(ec);
        }

        return result;
    }




    @Override public List<Board> guessAlternatives() { return getEdgeTuples(true).get(0).choices; }
    @Override public List<FlattenSolvableTuple<Board>> getSuccessorTuples() { return getEdgeTuples(false); }

}
