import grid.graph.GridGraph;
import grid.logic.LogicStatus;

import java.awt.*;
import java.util.Set;
import java.util.List;

/**
 * Created by chien on 11/6/2017.
 */
public class BlackConnectivityLogicStep implements grid.logic.LogicStep<Board>
{
    private class MyGridReference implements GridGraph.GridReference
    {
        int blackcount = 0;
        Board thing;
        public MyGridReference(Board thing) { this.thing = thing; }
        public int getWidth() { return thing.getWidth();}
        public int getHeight() { return thing.getHeight(); }
        public boolean isIncludedCell(int x, int y)
        {
            CellState cs = thing.getCell(x,y);
            if (cs == CellState.BLACK) ++blackcount;
            return cs != CellState.WHITE;
        }
        public boolean edgeExitsEast(int x, int y) { return true; }
        public boolean edgeExitsSouth(int x, int y) { return true; }
    }

    private class MyExcludingGridReference extends MyGridReference
    {
        Point exclude;
        public MyExcludingGridReference(Board b,Point p) { super(b); exclude = p; }
        public boolean isIncludedCell(int x,int y)
        {
            if (x == exclude.x && y == exclude.y) return false;
            return super.isIncludedCell(x,y);
        }
    }

    // given a list of connected-sets on a board with at least one black cell,
    // return the connected set that contains all black cells, or -1 if
    // the black cells are spread across multiple con-sets.
    private int getSingleBlackConSet(Board b, List<Set<Point>> consets)
    {
        int result = -1;
        for (int i = 0; i < consets.size() ; ++i)
        {
            boolean blackfound = false;
            for (Point p : consets.get(i))
            {
                if (b.getCell(p.x,p.y) == CellState.BLACK) { blackfound = true; break; }
            }
            if (blackfound == true)
            {
                if (result != -1) return -1;
                result = i;
            }
        }
        return result;
    }




    @Override
    public LogicStatus apply(Board thing)
    {
        LogicStatus result = LogicStatus.STYMIED;

        MyGridReference mgr = new MyGridReference(thing);
        GridGraph gg = new GridGraph(mgr);
        if (mgr.blackcount == 0) return LogicStatus.STYMIED;

        List<Set<Point>> consets = gg.connectedSets();

        if (consets.size() == 0 ) return LogicStatus.CONTRADICTION;
        if (consets.size() > 1)
        {
            int blackregion = getSingleBlackConSet(thing,consets);
            if (blackregion == -1) return LogicStatus.CONTRADICTION;

            // if we get here, all black cells are in one connected set, which is good.
            // any other connected set, by definition, contains only unknown cells (white cells weren't in the graph)
            // and all those unknown cells _cannot_ be black because they would then be disconnected.
            for (int i = 0 ; i < consets.size() ; ++i)
            {
                if (i == blackregion) continue;
                for (Point p  : consets.get(i)) thing.setCell(p.x,p.y,CellState.WHITE);
            }
            // if there were more than 1 conset, and at least one black cell, and all black cells are in one
            // conset, then by definition, there must have been one non-empty conset with no black, and therefore
            // unknown cells in it...
            result = LogicStatus.LOGICED;
        }

        // if we get _here_, then there either was already or after some work now is, only a single conset.
        // if we Logiced some additional white cells to make the single conset, we need to re-run the graph logic.
        if (result == LogicStatus.LOGICED) gg = new GridGraph(mgr);

        // this will throw an exception if I've screwed this logic up and there's some way for
        // the post-add-white grid graph to still have multiple consets (articulation point calculation
        // requires starting from a connected graph)
        Set<Point> arts = gg.getArticulationPoints();

        // for every unknown articulation point...if it is breaks the black set up, it must be a
        // black cell itself.
        for (Point art : arts)
        {
            if (thing.getCell(art.x,art.y) != CellState.UNKNOWN) continue;
            GridGraph ggsub = new GridGraph(new MyExcludingGridReference(thing,art));
            List<Set<Point>> consetsub = ggsub.connectedSets();
            if (getSingleBlackConSet(thing,consetsub) == -1)
            {
                thing.setCell(art.x,art.y,CellState.BLACK);
                result = LogicStatus.LOGICED;
            }


        }

        return result;
    }
}
