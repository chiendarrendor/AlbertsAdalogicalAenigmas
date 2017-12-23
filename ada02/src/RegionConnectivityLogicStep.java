import grid.graph.GridGraph;
import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by chien on 10/23/2017.
 */

// since any region may only be entered and exited once
// all cells with paths in the region must be connectable to
// each other completely within the region.
public class RegionConnectivityLogicStep implements LogicStep<Board>
{
    char regionid;
    public RegionConnectivityLogicStep(char regionid) { this.regionid = regionid; }

    private class MyRef implements GridGraph.GridReference
    {
        Board b;
        Set<Point> cset = new HashSet<Point>();
        Set<Point> pathset = new HashSet<Point>();
        public MyRef(Board b)
        {
            this.b = b;
            for (Point p : b.getRegion(regionid).getCells())
            {
                if (b.getPathState(p.x,p.y) != PathState.OFFPATH) cset.add(p);
                if (b.getPathState(p.x,p.y) == PathState.ONPATH) pathset.add(p);
            }
        }
        public int getWidth() { return b.getWidth(); }
        public int getHeight() { return b.getHeight(); }
        public boolean isIncludedCell(int x, int y)
        {
            return cset.contains(new Point(x,y));
        }
        public boolean edgeExitsEast(int x, int y) { return b.getEdge(x,y, Direction.EAST) != EdgeType.WALL; }
        public boolean edgeExitsSouth(int x, int y) { return b.getEdge(x,y,Direction.SOUTH) != EdgeType.WALL; }
    }

    public LogicStatus apply(Board thing)
    {
        int ecc = thing.getRegionExpectedCellCount(regionid);
        MyRef mr = new MyRef(thing);
        GridGraph gg = new GridGraph(mr);

        List<Set<Point>> consets = gg.connectedSets();
        Map<Point,Integer> pointMap = new HashMap<>();
        for (int i = 0 ; i < consets.size() ; ++i) { for (Point p : consets.get(i)) { pointMap.put(p,i); } }

        if (mr.pathset.size() > 1)
        {
            Set<Integer> onIndices = new HashSet<>();
            for (Point p : mr.pathset) { onIndices.add(pointMap.get(p)); }
            if (onIndices.size() > 1) return LogicStatus.CONTRADICTION;
            // now we know that all known onpath cells are in one region.
            int onConnSet = onIndices.iterator().next();
            if (ecc > 0 && consets.get(onConnSet).size() < ecc) return LogicStatus.CONTRADICTION;
            // if we get here, either the on connSet is big enough or we don't care what size it is.
            // all other cells should be not on path
            LogicStatus result = LogicStatus.STYMIED;
            for (int i = 0 ; i < consets.size() ; ++i)
            {
                if (i == onConnSet) continue;
                result = LogicStatus.LOGICED;
                for (Point p : consets.get(i)) thing.setPathState(p.x, p.y, PathState.OFFPATH);
            }
            return result;
        }

        // if we get here, we don't know which con-set is the one we want.
        // we can narrow it down if we have a region with a size;
        // any region smaller than the ecc can't be the one we want.
        if (ecc < 0) return LogicStatus.STYMIED;
        LogicStatus result = LogicStatus.STYMIED;

        for (Set<Point> conset : consets)
        {
            if (conset.size() >= ecc) continue;
            result = LogicStatus.LOGICED;
            for (Point p : conset) thing.setPathState(p.x,p.y,PathState.OFFPATH);
        }

        return result;
    }
}
