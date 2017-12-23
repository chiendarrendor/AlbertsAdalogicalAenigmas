import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.util.Vector;

/**
 * Created by chien on 7/24/2017.
 */
public class OutsideLogicStep implements LogicStep<LogicBoard>
{
    Board.CellInfo outside;

    public OutsideLogicStep(Board board)
    {
        outside = board.outside;
    }

    @Override
    public LogicStatus apply(LogicBoard thing)
    {
        int wallcount = 0;
        int pathcount = 0;
        Vector<Board.OutsidePair> unknowns = new Vector<>();

        for (Board.OutsidePair op : outside.opairs)
        {
            EdgeState es = thing.getBoard().getEdge(op.ci.x,op.ci.y,op.dir);
            switch(es)
            {
                case PATH: ++pathcount; break;
                case WALL: ++wallcount; break;
                case UNKNOWN: unknowns.add(op); break;
            }
        }

        if (pathcount > 2) return LogicStatus.CONTRADICTION;
        if (pathcount + unknowns.size() < 2) return LogicStatus.CONTRADICTION;
        if (unknowns.size() == 0) return LogicStatus.STYMIED;

        if (pathcount == 2)
        {
            for (Board.OutsidePair op : unknowns) { thing.getBoard().wall(op.ci.x,op.ci.y,op.dir); }
            return LogicStatus.LOGICED;

        }

        if (pathcount + unknowns.size() == 2)
        {
            for (Board.OutsidePair op: unknowns) { thing.getBoard().path(op.ci.x,op.ci.y,op.dir); }
            return LogicStatus.LOGICED;
        }

        return LogicStatus.STYMIED;
    }
}
