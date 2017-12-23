import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by chien on 10/18/2017.
 */
public class CellLogicStep implements LogicStep<Board>
{
    int x;
    int y;

    public CellLogicStep(int x, int y) { this.x = x; this.y = y;}

    public LogicStatus apply(Board thing)
    {
        int wallcount = 0;
        int pathcount = 0;
        Set<Direction> unknowns = new HashSet<>();

        for (Direction d : Direction.orthogonals())
        {
            switch(thing.getEdge(x,y,d))
            {
                case UNKNOWN: unknowns.add(d); break;
                case WALL: ++wallcount; break;
                case PATH: ++pathcount; break;
            }
        }

        // cases:

        // w4 p0 u0
        if (wallcount == 4)
        {
            PathState ps = thing.getPathState(x,y);
            if (ps == PathState.ONPATH) return LogicStatus.CONTRADICTION;
            if (ps == PathState.OFFPATH) return LogicStatus.STYMIED;
            thing.setPathState(x,y,PathState.OFFPATH);
            return LogicStatus.LOGICED;
        }
        // w3 p1 u0
        if (wallcount == 3 && pathcount == 1) return LogicStatus.CONTRADICTION;
        // w2 p2 u0
        if (wallcount == 2 && pathcount == 2)
        {
            if (thing.getPathState(x,y) == PathState.OFFPATH) return LogicStatus.CONTRADICTION;
            return LogicStatus.STYMIED;
        }
        // w0 p3 u1
        // w0 p4 u0
        // w1 p3 u0
        if (pathcount > 2) return LogicStatus.CONTRADICTION;

        // if we get here, we know we have some unknowns....
        // w3 p0 u1
        if (wallcount == 3)
        {
            for (Direction d : unknowns) { thing.setEdgeWall(x,y,d); }
            PathState ps = thing.getPathState(x,y);
            if (ps == PathState.ONPATH) return LogicStatus.CONTRADICTION;
            if (ps == PathState.OFFPATH) return LogicStatus.STYMIED;
            thing.setPathState(x,y,PathState.OFFPATH);
            return LogicStatus.LOGICED;
        }

        // w0 p2 u2
        // w1 p2 u1
        if (pathcount == 2)
        {
            for (Direction d : unknowns) { thing.setEdgeWall(x,y,d); }
            return LogicStatus.LOGICED;
        }

        // w2 p1 u1
        if (wallcount == 2 && pathcount == 1)
        {
            if (thing.getPathState(x,y) == PathState.OFFPATH) return LogicStatus.CONTRADICTION;
            for (Direction d: unknowns)
            {
                if (!thing.setEdgePath(x,y,d)) return LogicStatus.CONTRADICTION;
            }
            return LogicStatus.LOGICED;
        }

        // w2 p0 u2
        if (wallcount == 2)
        {
            PathState ps = thing.getPathState(x,y);
            if (ps == PathState.UNKNOWN) return LogicStatus.STYMIED;

            if (ps == PathState.OFFPATH)
            {
                for (Direction d : unknowns) { thing.setEdgeWall(x,y,d); }
            }
            else
            {
                for (Direction d: unknowns) { if (!thing.setEdgePath(x,y,d)) return LogicStatus.CONTRADICTION; }
            }
            return LogicStatus.LOGICED;
        }


        // w1 p1 u2 ?
        // w1 p0 u3 ?
        // w0 p1 u3 ?
        // w0 p0 u4 ?
        return LogicStatus.STYMIED;
    }
}
