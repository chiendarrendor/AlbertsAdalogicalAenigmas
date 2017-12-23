import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.*;
import java.util.Collection;

/**
 * Created by chien on 8/13/2017.
 */
public class CellLogicStep implements LogicStep<LogicBoard>
{
    int x;
    int y;
    Collection<Point> adjacents;

    public CellLogicStep(Board b, int x, int y) { this.x = x; this.y = y; adjacents = b.getAdjacentCells(x,y); }

    @Override
    public LogicStatus apply(LogicBoard thing)
    {
        LogicStatus result = LogicStatus.STYMIED;
        Board.IntegerSet myis = thing.getPossibles(x,y);


        for (Point adj : adjacents )
        {
            Board.IntegerSet adjis = thing.getPossibles(adj.x,adj.y);
            if (!adjis.isSingular()) continue;
            int adjval = adjis.getSingular();

            if (!myis.contains(adjval)) continue;
            result = LogicStatus.LOGICED;
            myis.remove(adjval);
        }

        if (myis.size() == 0) return LogicStatus.CONTRADICTION;
        return result;
    }
}
