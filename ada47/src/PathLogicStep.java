import grid.logic.LogicStatus;

import java.util.Set;

/**
 * Created by chien on 9/4/2017.
 */
public class PathLogicStep implements grid.logic.LogicStep<LogicBoard>
{
    @Override
    public LogicStatus apply(LogicBoard thing)
    {
        PathSet pso = thing.ps;
        Set<Path> ps = pso.paths;

        pso.MergeAll();

        int closedcount = 0;

        for (Path p : ps)
        {
            if (p.isClosed) ++closedcount;
        }
        if (closedcount > 1) return LogicStatus.CONTRADICTION;
        if (closedcount == 1 && ps.size() > 1) return LogicStatus.CONTRADICTION;
        return LogicStatus.STYMIED;
    }
}
