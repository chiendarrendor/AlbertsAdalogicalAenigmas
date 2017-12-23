import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

/**
 * Created by chien on 10/19/2017.
 */
public class AdjacentCellLogicStep implements LogicStep<Board>
{
    int x1,y1,x2,y2;
    Direction d;
    public AdjacentCellLogicStep(int x1, int y1, int x2, int y2,Direction d)
    {
        this.x1 = x1; this.y1 = y1;
        this.x2 = x2; this.y2 = y2;
        this.d = d;
    }

    // this class will detect if either this cell or the other one have non-path path states, and if
    // so, put a wall between them.
    public LogicStatus apply(Board thing)
    {
        PathState ps1 = thing.getPathState(x1,y1);
        PathState ps2 = thing.getPathState(x2,y2);
        EdgeType es = thing.getEdge(x1,y1,d);
        if (ps1 == PathState.OFFPATH || ps2 == PathState.OFFPATH)
        {
            switch(es)
            {
                case UNKNOWN: thing.setEdgeWall(x1,y1,d); return LogicStatus.LOGICED;
                case WALL: return LogicStatus.STYMIED;
                case PATH: return LogicStatus.CONTRADICTION;
            }
        }

        return LogicStatus.STYMIED;
    }
}
