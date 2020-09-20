import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.lambda.CellLambda;
import grid.lambda.XYLambda;
import grid.logic.LogicStatus;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;
import grid.puzzlebits.Path.GridPathContainer;
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

public class Board extends SingleLoopBoard<Board> {
    @Shallow GridFileReader gfr;
    @Shallow Map<Character,List<Point>> regions = new HashMap<>();
    @Shallow Map<Character,Integer> clueRegions = new HashMap<>();

    private List<Point> getRegionList(char c) {
        if (!regions.containsKey(c)) {
            regions.put(c,new ArrayList<Point>());
        }
        return regions.get(c);
    }


    public Board(String fname) {
        gfr = new GridFileReader(fname);
        forEachCell((x,y)-> {
            getRegionList(getRegionId(x,y)).add(new Point(x,y));
            if (isClue(x,y)) {
                if (clueRegions.containsKey(getRegionId(x, y))) throw new RuntimeException("Duplicate clue in region");
                clueRegions.put(getRegionId(x, y),getClue(x,y));
            }
        });
        init();
    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public char getRegionId(int x, int y) { return gfr.getBlock("REGIONS")[x][y].charAt(0); }
    public boolean isClue(int x, int y) { return Character.isDigit(gfr.getBlock("CLUELETTERS")[x][y].charAt(0)); }
    public boolean isLetter(int x,int y) { return !isClue(x,y); }
    public char getLetter(int x,int y) { return gfr.getBlock("CLUELETTERS")[x][y].charAt(0); }
    public int getClue(int x,int y) { return Integer.parseInt(gfr.getBlock("CLUELETTERS")[x][y]); }
    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }

    public Collection<Character> getRegionIds() { return regions.keySet(); }
    public Collection<Point> getRegionCells(char rid) { return regions.get(rid); }
    public boolean regionHasClue(char rid) { return clueRegions.containsKey(rid); }
    public int getRegionClue(char rid) { return clueRegions.get(rid); }
    public GridPathContainer getPathContainer() { return super.getPathContainer(); }

    public static class MyMove {
        int x;
        int y;
        boolean isV;
        EdgeState es;
        public MyMove(int x,int y,boolean isV,EdgeState es) { this.x = x; this.y = y; this.isV = isV; this.es = es; }

        public boolean applyMove(Board b) {
            if (b.getEdge(x,y,isV) != EdgeState.UNKNOWN) return b.getEdge(x,y,isV) == es;
            b.setEdge(x,y,isV,es);
            return true;
        }
    }



    @Override public boolean isComplete() { return this.getUnknownCount() == 0; }
    @Override public boolean applyMove(Object o) { return ((MyMove)o).applyMove(this); }

    private List<FlattenSolvableTuple<Board>> getRawTuples(boolean onlyone) {
        List<FlattenSolvableTuple<Board>> result = new ArrayList<>();
        forEachEdge((x,y,isV,es)->{
            if (es != EdgeState.UNKNOWN) return;
            if (onlyone && result.size() > 0) return;
            MyMove mm1 = new MyMove(x,y,isV,EdgeState.PATH);
            MyMove mm2 = new MyMove(x,y,isV,EdgeState.WALL);
            Board b1 = new Board(this);
            Board b2 = new Board(this);
            mm1.applyMove(b1);
            mm2.applyMove(b2);
            result.add(new FlattenSolvableTuple<Board>(b1,mm1,b2,mm2));
        });
        return result;
    }

    @Override public List<FlattenSolvableTuple<Board>> getSuccessorTuples() { return getRawTuples(false); }
    @Override public List<Board> guessAlternatives() { return getRawTuples(true).get(0).choices; }

    public CellProcessor processCell(int x,int y,CellType initialCellType) { return new CellProcessor(x,y,initialCellType); }



    public class CellProcessor {
        CellType ct = CellType.UNKNOWN;
        LogicStatus result = LogicStatus.STYMIED;
        int x;
        int y;
        private CellProcessor(int x,int y,CellType initialCellType) {
            this.x = x;
            this.y = y;
            List<Direction> paths = new ArrayList<>();
            List<Direction> walls = new ArrayList<>();
            List<Direction> unknowns = new ArrayList<>();
            for (Direction d : Direction.orthogonals()) {
                switch(getEdge(x,y,d)) {
                    case PATH: paths.add(d); break;
                    case WALL: walls.add(d); break;
                    case UNKNOWN: unknowns.add(d); break;
                }
            }

            // walls   paths
            //          0       1       2       3       4
            //      0                   2       B       B
            //      1                   2       B       X
            //      2                   2       X       X
            //      3   3       3       X       X       X
            //      4   A       X       X       X       X
            // X = not possible (more than 4 walls/paths/unknowns)
            // A = not legal (every cell must be part of a path)
            // B = not legal (no cell can have more than 2 entries
            // 2 ... must be part of an internal...all other edges must be walls, and if initialType is terminal, it's wrong
            //   ... also, paths must be opposite each other.
            // 3 ... must be a terminal...open edge must be path, and if initialType is internal, it's wrong.

            if (walls.size() == 4) { result = LogicStatus.CONTRADICTION; return; } // A
            if (paths.size() > 2) { result = LogicStatus.CONTRADICTION; return; } // B

            if (paths.size() == 2) {
                if (initialCellType == CellType.TERMINAL) { result = LogicStatus.CONTRADICTION ; return; }
                if (paths.get(0) != paths.get(1).getOpp()) { result = LogicStatus.CONTRADICTION; return; }
                ct = CellType.INTERNAL;
                if (unknowns.size() == 0) { return; }
                result = LogicStatus.LOGICED;
                unknowns.stream().forEach(d->setEdge(x,y,d,EdgeState.WALL));
                return;
            }

            if (walls.size() == 3) {
                if (initialCellType == CellType.INTERNAL) { result = LogicStatus.CONTRADICTION ; return; }
                ct = CellType.TERMINAL;
                if (unknowns.size() == 0) return;
                result = LogicStatus.LOGICED;
                unknowns.stream().forEach(d->setEdge(x,y,d,EdgeState.PATH));
                return;
            }

            switch(initialCellType) {
                case UNKNOWN:
                    // walls        paths
                    //          0       1
                    //     0
                    //     1            F
                    //     2    G       F
                    // F = a path opposite a wall means this is a TERMINAL and can be treated as such
                    // G = two adjacent walls with no path mean this must be a terminal
                    if (paths.size() == 1 && getEdge(x,y,paths.get(0).getOpp()) == EdgeState.WALL) {
                        ct = CellType.TERMINAL;
                        unknowns.stream().forEach(d->setEdge(x,y,d,EdgeState.WALL));
                        result = LogicStatus.LOGICED;
                        return;
                    }

                    if (walls.size() == 2 && paths.size() == 0 && walls.get(0).getOpp() != walls.get(1)) {
                        ct = CellType.TERMINAL;
                        return;
                    }

                    // no other results are possible
                    return;
                case TERMINAL:
                    // walls        paths
                    //          0       1
                    //     0            J
                    //     1            J
                    //     2            J
                    // J = one path means we're done...all other edges are walls
                    ct = CellType.TERMINAL;
                    if (paths.size() == 1) {
                        unknowns.stream().forEach(d->setEdge(x,y,d,EdgeState.WALL));
                        result = LogicStatus.LOGICED;
                        return;
                    }
                    // don't think we know anything in another case.
                    return;

                case INTERNAL:
                    // walls        paths
                    //          0       1
                    //     0            R
                    //     1            R
                    //     2    Q       Q
                    // Q = all other edges are paths, as long as the two walls are opposite...if adjacent, contradiction
                    // R edge opposite to path must also be path...it is wall, contradiction
                    ct = CellType.INTERNAL;
                    if (walls.size() == 2) {
                        if (walls.get(0) != walls.get(1).getOpp()) {
                            result = LogicStatus.CONTRADICTION;
                            return;
                        }
                        unknowns.stream().forEach(d -> setEdge(x, y, d, EdgeState.PATH));
                        result = LogicStatus.LOGICED;
                        return;
                    }
                    if (paths.size() == 1) {
                        Direction pdir = paths.get(0);
                        if (getEdge(x,y,pdir.getOpp()) == EdgeState.WALL) { result = LogicStatus.CONTRADICTION; return; }
                        result = LogicStatus.LOGICED;
                        setEdge(x,y,pdir.getOpp(),EdgeState.PATH);
                        for (Direction d : unknowns) {
                            if (getEdge(x,y,d) == EdgeState.UNKNOWN) setEdge(x,y,d,EdgeState.WALL);
                        }
                        return;
                    }
                    // I don't think we know enough to do anything here.
                    return;
            }







        }

    }





}
