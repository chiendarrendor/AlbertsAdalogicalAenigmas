import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import sun.rmi.runtime.Log;

/**
 * Created by chien on 10/18/2017.
 */
public class AdjacentCellCrossRegionLogicStep implements LogicStep<Board>
{
    int x1;
    int y1;
    int x2;
    int y2;


    public AdjacentCellCrossRegionLogicStep(int x1, int y1, int x2, int y2)
    {
        this.x1 = x1; this.y1 = y1;
        this.x2 = x2; this.y2 = y2;
    }

    public LogicStatus apply(Board thing)
    {
        // if we are here, then these two cells are in different regions.
        // as such, if one of them is not on a path, the other one _must_ be
        // on a path
        PathState ps1 = thing.getPathState(x1,y1);
        PathState ps2 = thing.getPathState(x2,y2);

        // ps1 ps2
        // OFF     OFF
        if (ps1 == PathState.OFFPATH && ps2 == PathState.OFFPATH) return LogicStatus.CONTRADICTION;
        // OFF     ON
        // ON      ON
        // ON      OFF
        if (ps1 != PathState.UNKNOWN && ps2 != PathState.UNKNOWN) return LogicStatus.STYMIED;
        // OFF     UNKNOWN
        if (ps1 == PathState.OFFPATH)
        {
            thing.setPathState(x2,y2,PathState.ONPATH);
            return LogicStatus.LOGICED;
        }
        // UNKNOWN OFF
        if (ps2 == PathState.OFFPATH)
        {
            thing.setPathState(x1,y1,PathState.ONPATH);
            return LogicStatus.LOGICED;
        }

        // UNKNOWN UNKNOWN
        // UNKNOWN ON
        // ON      UNKNOWN

        return LogicStatus.STYMIED;
    }
}
