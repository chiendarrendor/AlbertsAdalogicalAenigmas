import grid.logic.LogicStatus;
import grid.puzzlebits.Path.Path;

public class WholePathLogicStep implements grid.logic.LogicStep<Board>
{
    @Override
    public LogicStatus apply(Board thing) {


        try
        {
            thing.gpc.clean();
        }
        catch(BadMergeException bme)
        {
            return LogicStatus.CONTRADICTION;
        }


        int pcount = 0;
        int lcount = 0;

        for (Path p : thing.gpc)
        {
            ++pcount;
            if (p.isClosed()) ++lcount;
        }

        if (lcount > 1) return LogicStatus.CONTRADICTION;
        if (lcount == 1 && pcount > 1) return LogicStatus.CONTRADICTION;
        return LogicStatus.STYMIED;
    }
}
