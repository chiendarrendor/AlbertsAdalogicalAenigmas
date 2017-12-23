import grid.graph.GridGraph;
import grid.logic.LogicStatus;

import java.awt.*;
import java.util.*;

/**
 * Created by chien on 8/17/2017.
 */
public class ConnectivityLogicStep implements grid.logic.LogicStep<LogicBoard>
{
    private class MyGridReference implements GridGraph.GridReference
    {
        LogicBoard lb;
        Point extra = null;

        public MyGridReference(LogicBoard thing)
        {
            lb = thing;
        }
        public MyGridReference(LogicBoard thing,Point extraLand) { lb = thing; extra = extraLand; }


        public int getWidth()
        {
            return lb.getWidth();
        }
        public int getHeight()
        {
            return lb.getHeight();
        }
        public boolean isIncludedCell(int x, int y)
        {
            if (extra != null && extra.x == x && extra.y == y) return false;
            return lb.getCell(x,y) != CellState.LAND;
        }
        public boolean edgeExitsEast(int x, int y) { return true; }
        public boolean edgeExitsSouth(int x, int y) { return true; }
    }


    public LogicStatus apply(LogicBoard thing)
    {
        LogicStatus result = LogicStatus.STYMIED;

        GridGraph gg = new GridGraph(new MyGridReference(thing));
        if (!gg.isConnected())
        {
            // naively this is a contradiction.   However, the only sets that have to be connected
            // are ones that actually have water in them.  if some connected sets have water, and some don't
            // it actually turns out to be valid for the connected sets with no water to be filled in.
            int waterycount = 0;
            Vector<Set<Point>> drysets = new Vector<>();
            for (Set<Point> pset : gg.connectedSets())
            {
                if (hasWater(pset,thing)) ++waterycount;
                else drysets.add(pset);
            }

            // if we don't know anything about any water, we can't hazard a guess....
            if (waterycount == 0) return LogicStatus.STYMIED;
            // if all sets have water, this is a contradiction
            if (drysets.size() == 0) return LogicStatus.CONTRADICTION;

            for (Set<Point> dryset : drysets)
            {
                for (Point p : dryset)
                {
                    if (thing.getCell(p.x,p.y) != CellState.UNKWNOWN) continue;
                    result = LogicStatus.LOGICED;
                    thing.setCellLand(p.x,p.y);
                }
            }
            return result;
        }

        Set<Point> arts = gg.getArticulationPoints();


        for (Point p : arts)
        {
            if (thing.getCell(p.x,p.y) != CellState.UNKWNOWN) continue;

            // so we are faced with an unknown cell that is an articulation point.
            // if we re-run the connectivity with it removed, we should get
            // two (possibly more) disconnected sets.  only if at least two sets actually
            // have real water in them can we be sure that this has to be water
            // (otherwise, a set could just fill with land and we wouldn't need this space to be water)
            GridGraph artgg = new GridGraph(new MyGridReference(thing,p));
            if (artgg.isConnected()) throw new RuntimeException("That was unexpected");

            java.util.List<Set<Point>> sets = artgg.connectedSets();

            int watercount = 0;
            for (Set<Point> pset : sets) { if (hasWater(pset,thing)) ++watercount; }
            if (watercount < 2) continue;

            result = LogicStatus.LOGICED;
            thing.setCellRiver(p.x,p.y);
        }

        return result;
    }

    private boolean hasWater(Set<Point> pset, LogicBoard thing)
    {
        for (Point p : pset)
        {
            if (thing.getCell(p.x,p.y) == CellState.RIVER) return true;
        }
        return false;
    }


}
