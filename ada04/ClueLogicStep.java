import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import sun.rmi.runtime.Log;

import java.awt.*;

/**
 * Created by chien on 6/16/2017.
 */
public class ClueLogicStep implements LogicStep<Board>
{
    int idx;


    public ClueLogicStep(int i) { idx = i;}

    @Override
    public LogicStatus apply(Board thing)
    {
        Board.Clue c = thing.getClue(idx);
        if (c.isDone()) return LogicStatus.STYMIED;


        for (Direction d : c.validDirs())
        {
            Point p = d.goDir(c.getCur());
            if (!c.canLandNext(p.x,p.y))
            {
                c.clearDirection(d);
                return LogicStatus.LOGICED;
            }
        }

        if (c.validDirs().size() == 0)
        {
            return LogicStatus.CONTRADICTION;
        }
        if (c.validDirs().size() > 1) return LogicStatus.STYMIED;
        c.go(c.validDirs().iterator().next());
        return LogicStatus.LOGICED;
    }
}
