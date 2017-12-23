import grid.logic.LogicStatus;
import grid.puzzlebits.Direction;

import java.awt.*;

/**
 * Created by chien on 10/18/2017.
 */
public class SingleLoopLogicStep implements grid.logic.LogicStep<Board>
{
    public LogicStatus apply(Board thing)
    {
        PathManager pm = thing.getPathManager();

        if (pm.numClosedLoops() > 1) return LogicStatus.CONTRADICTION;
        if (pm.numClosedLoops() == 1 && pm.numPaths() > 1) return LogicStatus.CONTRADICTION;
        if (thing.isFilled() && !pm.isLoopPerfect()) return LogicStatus.CONTRADICTION;


        return LogicStatus.STYMIED;
    }
}
