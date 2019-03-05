import grid.graph.GGFrame;
import grid.graph.GridGraph;
import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RegionLogicStep implements LogicStep<Board> {
    int regionid;
    public RegionLogicStep(Region r) { regionid = r.getId(); }
    @Override public String toString() { return "RegionLogicStep" + regionid; }

    private static class MyGridReference implements GridGraph.GridReference {
        Board b;
        Region r;
        public MyGridReference(Board b,Region r) { this.b = b; this.r = r; }
        @Override public int getWidth() { return b.getWidth(); }
        @Override public int getHeight() { return b.getHeight(); }
        @Override public boolean edgeExitsEast(int x, int y) { return ! b.isEnemyWall(x,y, Direction.EAST); }
        @Override public boolean edgeExitsSouth(int x, int y) { return ! b.isEnemyWall(x,y,Direction.SOUTH); }

        @Override public boolean isIncludedCell(int x, int y) {
            Region curr = b.getRegionByCell(x,y);
            if (r == curr) return true;
            if (r.hasEnemy(new Point(x,y))) return false;
            if (curr == null) return true;
            return r.numbersMatch(curr);
        }
    }

    private static class SetGridReference implements GridGraph.GridReference {
        Board b;
        Set<Point> pset;
        Point artic;
        public SetGridReference(Board b, Set<Point> pset,Point artic) { this.b = b; this.pset = pset; this.artic = artic; }
        @Override public int getWidth() { return b.getWidth(); }
        @Override public int getHeight() { return b.getHeight(); }
        @Override public boolean edgeExitsEast(int x, int y) { return ! b.isEnemyWall(x,y,Direction.EAST); }
        @Override public boolean edgeExitsSouth(int x, int y) { return ! b.isEnemyWall(x,y,Direction.SOUTH); }

        @Override public boolean isIncludedCell(int x, int y) {
            if (artic != null && artic.x == x && artic.y == y) return false;
            return pset.contains(new Point(x,y));
        }
    }

    private static Set<Set<Point>> connectedSetsOfRegion(GridGraph gg,Region r) {
        Set<Set<Point>> consets = new HashSet<>();
        for(Point p : r.cellIter()) {
            consets.add(gg.connectedSetOf(p));
        }
        return consets;
    }

    private static boolean doFrame = false;
    GGFrame ggf = new GGFrame();

    @Override public LogicStatus apply(Board thing) {
        Region r = thing.getRegionById(regionid);

        if (!r.isActive()) return LogicStatus.STYMIED;

        // gets rid of inactive regions (no cells)
        //                                   0 1 2 3 4 5 6
        // cases:   size 4, actualsize -1:   X 1 2 3 4 5 6
        //          size 4, actualsize 3:    X 1 2 3 4 5 6
        //          size 4, actualsize 5:    X 1 2 3 4 5 6

        if (r.isDone()) return LogicStatus.STYMIED;

        // gets rid of cases where we are done.
        //                                   0 1 2 3 4 5 6
        // cases:   size 4, actualsize -1:   X 1 2 3 4 5 6
        //          size 4, actualsize 3:    X 1 2 Y 4 5 6
        //          size 4, actualsize 5:    X 1 2 3 4 Y 6



        if (r.numCells() > r.larger()) return LogicStatus.CONTRADICTION;

        // gets rid of cases where region is too big.
        //                                   0 1 2 3 4 5 6
        // cases:   size 4, actualsize -1:   X 1 2 3 4 5 Z
        //          size 4, actualsize 3:    X 1 2 Y Z Z Z
        //          size 4, actualsize 5:    X 1 2 3 4 Y Z




        // if numcells was equal to larger, and actualsize was not -1, then we'd be isDone
        if (r.numCells() == r.larger() && r.getActualSize() == -1) {
            thing.setActualSize(regionid,r.larger());
            return LogicStatus.LOGICED;
        }

        // gets rid of the case where number of cells is equal to larger and we didn't know before now
        // what our actualnum was.
        //                                   0 1 2 3 4 5 6
        // cases:   size 4, actualsize -1:   X 1 2 3 4 A Z
        //          size 4, actualsize 3:    X 1 2 Y Z Z Z
        //          size 4, actualsize 5:    X 1 2 3 4 Y Z

        // resultant invariant:
        // region is active
        // if we know what our actualsize is, we don't have enough cells.
        // if we don't know what our actualsize is, we might have enough cells for smaller, but we don't have enough for larger


        // out of all the spaces on the board that are in our region, null, or belonging to regions
        // where numbersmatch, hopefully all of our regions spaces fall into the same connected set.
        // it's a contradiction if our region fails to do that.
        GridGraph gg = new GridGraph(new MyGridReference(thing,r));
        if (doFrame) ggf.addGridGraph(gg,"Conset detector").addSpecial(Color.RED,r.cellSet());

        Set<Set<Point>> regsets = connectedSetsOfRegion(gg,r);
        if (regsets.size() > 1) return LogicStatus.CONTRADICTION;
        Set<Point> regset = regsets.iterator().next();

        Set<Point> adjs = r.getAdjacents(thing,true);


        if (adjs.size() == 0) {
            // if we know what our actual size is, we needed another cell and can't have one.
            if (r.getActualSize() != -1) return LogicStatus.CONTRADICTION;
            // if we get here, we don't know what our actual size is, but
            // we can't get bigger, we can't get smaller, so our only hope is if we are the right size for smaller.
            if (r.smaller() == r.numCells()) {
                thing.setActualSize(regionid,r.smaller());
                return LogicStatus.LOGICED;
            }
            // if we get here, then we're either larger or smaller than smaller, and we fail.
            return LogicStatus.CONTRADICTION;
        }

        LogicStatus result = LogicStatus.STYMIED;
        if (adjs.size() == 1 && r.numCells() < r.smaller()) {
            thing.extendInto(regionid,adjs.iterator().next());
            result = LogicStatus.LOGICED;
        }


        // regset is the connected set containing all points of this region and that this
        // region could theoretically expand into
        if (r.getActualSize() == -1 && regset.size() < r.larger()) {
            thing.setActualSize(regionid,r.smaller());
            result = LogicStatus.LOGICED;
        }

        if (r.getActualSize() != -1 && regset.size() < r.getActualSize()) {
            return LogicStatus.CONTRADICTION;
        }

        if (r.getActualSize() != -1 && regset.size() == r.getActualSize()) {
            // this bit here is because we only want to add a region once..
            Set<Point> nulls = new HashSet<>();
            Set<Integer> others = new HashSet<>();
            for (Point p : regset) {
                Region pr = thing.getRegionByCell(p.x,p.y);
                if (pr == null) { nulls.add(p); continue; }
                if (pr.getId() == regionid) continue;
                others.add(pr.getId());
            }
            for (Point p : nulls) {
                if (!thing.canExtendInto(regionid,p)) return LogicStatus.CONTRADICTION;
                thing.extendInto(regionid,p);
                result = LogicStatus.LOGICED;
            }
            for (int rid : others) {
                if (!thing.canExtendInto(regionid,thing.getRegionById(rid).cellIter().iterator().next())) return LogicStatus.CONTRADICTION;
                thing.extendInto(regionid,thing.getRegionById(rid).cellIter().iterator().next());
                result = LogicStatus.LOGICED;
            }

        }

        adjs = r.getAdjacents(thing,true);

        // given only the set of cells that are points of this region and extendable-to cells
        // let's find articulation points.
        GridGraph ggr = new GridGraph(new SetGridReference(thing,regset,null));
        if (doFrame) {
            Set<Point> curs = new HashSet<>();
            curs.addAll(r.cellSet());
            ggf.addGridGraph(ggr,"Articulator").addSpecial(Color.GREEN,curs);


        }
        Set<Point> arts = ggr.getArticulationPoints();

        for (Point p : arts) {
            if (r.hasCell(p)) continue;

            List<Set<Point>> rawconsets = ggr.getArticulationSet(p);
            List<Set<Point>> consets = new ArrayList<>();

            for (Set<Point> conset : rawconsets) {
                if (conset.stream().anyMatch(cp->r.cellSet().contains(cp))) consets.add(conset);
            }

            if (consets.size() > 1) {
                thing.extendInto(regionid,p);
                result = LogicStatus.LOGICED;
                continue;
            }

            Set<Point> myconset = consets.iterator().next();
            if (myconset.size() < r.smaller()) {
                thing.extendInto(regionid,p);
                result = LogicStatus.LOGICED;
            }
        }



        return result;
    }
}
