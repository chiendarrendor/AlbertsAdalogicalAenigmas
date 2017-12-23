import grid.logic.simple.LogicStatus;
import grid.logic.LogicStep;

import java.awt.*;
import java.util.Vector;

/**
 * Created by chien on 5/19/2017.
 */
public class NoAdjacentLogicStep implements LogicStep<Board>
{
    @Override
    public LogicStatus apply(Board thing)
    {
        LogicStatus result = LogicStatus.STYMIED;

        for(Point p : thing.getBlackCells())
        {
            LogicStatus stat = applyToOne(thing,p);
            if (stat == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
            if (stat == LogicStatus.LOGICED) result = LogicStatus.LOGICED;
        }
        return result;
    }

    private LogicStatus applyToOne(Board thing, Point p)
    {
        Vector<Point> unknowns = new Vector<>();

        for(Point adj : thing.adjacents(p.x,p.y))
        {
            switch(thing.getCell(adj.x,adj.y))
            {
                case UNKNOWN:
                    unknowns.add(adj);
                    break;
                case BLACK:
                    return LogicStatus.CONTRADICTION;
                case WHITE:
                    break;
            }
        }

        if (unknowns.size() == 0) return LogicStatus.STYMIED;
        for (Point unk : unknowns)
        {
            thing.setCellWhite(unk.x,unk.y);
        }
        return LogicStatus.LOGICED;
    }
}
