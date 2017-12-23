import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.util.Vector;

/**
 * Created by chien on 10/18/2017.
 */
public class RegionEntryExitLogicStep implements LogicStep<Board>
{
    char rid;
    public RegionEntryExitLogicStep(char rid) { this.rid = rid; }


    // a region should have exactly one entry and one exit.
    public LogicStatus apply(Board thing)
    {
        Region r = thing.getRegion(rid);
        int wallcount = 0;
        int pathcount = 0;
        Vector<Region.EdgeTuple> unknowns = new Vector<>();

        for (Region.EdgeTuple et : r.getEdgeTuples() )
        {
            switch(thing.getEdge(et.x,et.y,et.d))
            {
                case UNKNOWN: unknowns.add(et); break;
                case WALL: ++wallcount; break;
                case PATH: ++pathcount; break;
            }
        }

        if (pathcount > 2) return LogicStatus.CONTRADICTION;
        if (pathcount + unknowns.size() < 2) return LogicStatus.CONTRADICTION;
        if (unknowns.size() == 0) return LogicStatus.STYMIED;

        if (pathcount == 2)
        {
            for(Region.EdgeTuple et : unknowns) thing.setEdgeWall(et.x,et.y,et.d);
            return LogicStatus.LOGICED;
        }

        if (pathcount + unknowns.size() == 2)
        {
            for(Region.EdgeTuple et : unknowns) thing.setEdgePath(et.x,et.y,et.d);
            return LogicStatus.LOGICED;
        }

        return LogicStatus.STYMIED;
    }
}
