import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.util.Vector;

/**
 * Created by chien on 7/24/2017.
 */
public class OptionalCellLogicStep implements LogicStep<LogicBoard>
{
    Board.CellInfo ci;


    public OptionalCellLogicStep(int x, int y, Board board)
    {
        ci = board.getCI(x,y);
    }

    public String toString() { return "OptionalCellLogicStep (" + ci.x + "," + ci.y + ")"; }


    @Override
    public LogicStatus apply(LogicBoard thing)
    {
        int wallcount = 0;
        int pathcount = 0;
        Vector<Direction> unknowns = new Vector<Direction>();

        for (Direction dir : Direction.orthogonals())
        {
            EdgeState es = thing.getBoard().getEdge(ci.x, ci.y, dir);
            switch (es)
            {
                case PATH:
                    ++pathcount;
                    break;
                case WALL:
                    ++wallcount;
                    break;
                case UNKNOWN:
                    unknowns.add(dir);
                    break;
            }
        }

        if (wallcount == 4)
        {
            return LogicStatus.STYMIED;
        }
        if (wallcount == 3 && pathcount == 1) return LogicStatus.CONTRADICTION;
        // if pathcount isn't 1, then unknowns.size must be 1
        if (wallcount == 3)
        {
            thing.getBoard().wall(ci.x,ci.y,unknowns.iterator().next());
            return LogicStatus.LOGICED;
        }

        // cases for wallcount = 2:
        // pathcount   unknown.size   and?
        //     2             0        2 walls, 2 paths, legal.
        //     1             1        2 walls, 1 path...the unknown must be a path
        //     0             2        unknowns could be either walls or paths, stymied.
        if (wallcount == 2 && pathcount == 1)
        {
            thing.getBoard().path(ci.x,ci.y,unknowns.iterator().next());
            return LogicStatus.LOGICED;
        }

        if (wallcount == 2) return LogicStatus.STYMIED;

        if (pathcount > 2) return LogicStatus.CONTRADICTION;
        if (pathcount == 2 && wallcount < 2)
        {
            for(Direction dir : unknowns) thing.getBoard().wall(ci.x,ci.y,dir);
            return LogicStatus.LOGICED;
        }

        return LogicStatus.STYMIED;


    }
}
