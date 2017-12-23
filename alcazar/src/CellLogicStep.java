import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;
import sun.rmi.runtime.Log;

import java.util.Vector;

/**
 * Created by chien on 7/24/2017.
 */
public class CellLogicStep implements LogicStep<LogicBoard>
{
    Board.CellInfo ci;

    public String toString() { return "CellLogicStep (" + ci.x + "," + ci.y + ")"; }


    public CellLogicStep(int x, int y, Board board)
    {
        ci = board.getCI(x,y);
    }

    @Override
    public LogicStatus apply(LogicBoard thing)
    {
        int wallcount = 0;
        int pathcount = 0;
        Vector<Direction> unknowns = new Vector<Direction>();

        for (Direction dir : Direction.orthogonals())
        {
            EdgeState es = thing.getBoard().getEdge(ci.x,ci.y,dir);
            switch(es)
            {
                case PATH: ++pathcount; break;
                case WALL: ++wallcount; break;
                case UNKNOWN: unknowns.add(dir); break;
            }
        }


        if (pathcount > 2) return LogicStatus.CONTRADICTION;
        if (pathcount + unknowns.size() < 2) return LogicStatus.CONTRADICTION;
        if (unknowns.size() == 0) return LogicStatus.STYMIED;

        if (pathcount == 2)
        {
            for (Direction dir : unknowns) { thing.getBoard().wall(ci.x,ci.y,dir); }
            return LogicStatus.LOGICED;

        }

        if (pathcount + unknowns.size() == 2)
        {
            for (Direction dir: unknowns) { thing.getBoard().path(ci.x,ci.y,dir); }
            return LogicStatus.LOGICED;
        }

        return LogicStatus.STYMIED;
    }
}
