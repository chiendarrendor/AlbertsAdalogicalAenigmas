import grid.logic.LogicStatus;
import grid.puzzlebits.Path.Path;

import java.util.List;

public class SinglePathLogicStep implements grid.logic.LogicStep<Board>
{
    public LogicStatus apply(Board thing)
    {
        try
        {
            thing.gpc.clean();
        }
        catch(BadMergeException bme)
        {
            return LogicStatus.CONTRADICTION;
        }

        for (Path p : thing.gpc)
        {
            if (p.isClosed()) return LogicStatus.CONTRADICTION;
        }

        // the rules of the game indicate that you have to have a single path that starts
        // and ends at the two terminals.
        // with all onpath cells (other than the terminals) required to have 2 path edges,
        // the only way to close off some cells so they can't get to the terminals is for
        // them to join to a loop....
        return LogicStatus.STYMIED;
    }
}
