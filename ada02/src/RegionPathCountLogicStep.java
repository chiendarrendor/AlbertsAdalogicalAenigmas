import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import sun.rmi.runtime.Log;

import java.awt.*;
import java.util.Vector;

/**
 * Created by chien on 10/18/2017.
 */
public class RegionPathCountLogicStep implements LogicStep<Board>
{
    char regionid;
    int cellcount;

    public RegionPathCountLogicStep(char rid, int cc)
    {
        regionid = rid;
        cellcount = cc;
    }

    // we are here if and only if this region has a non-zero cell count
    // _exactly_ that many cells must be path cells in this region.
    public LogicStatus apply(Board thing)
    {
        Region r = thing.getRegion(regionid);
        int nonpathcount = 0;
        int pathcount = 0;
        Vector<Point> unknowns = new Vector<>();
        for (Point p : r.getCells())
        {
            switch(thing.getPathState(p.x,p.y))
            {
                case UNKNOWN: unknowns.add(p);break;
                case ONPATH: ++pathcount; break;
                case OFFPATH: ++nonpathcount; break;
            }
        }

        if (pathcount > cellcount) return LogicStatus.CONTRADICTION;
        if (pathcount + unknowns.size() < cellcount) return LogicStatus.CONTRADICTION;
        if (unknowns.size() == 0) return LogicStatus.STYMIED;

        if (pathcount == cellcount)
        {
            for (Point p : unknowns) thing.setPathState(p.x,p.y,PathState.OFFPATH);
            return LogicStatus.LOGICED;
        }

        if (pathcount + unknowns.size() == cellcount)
        {
            for (Point p : unknowns) thing.setPathState(p.x,p.y,PathState.ONPATH);
            return LogicStatus.LOGICED;
        }

        return LogicStatus.STYMIED;
    }
}
