import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.util.Vector;

/**
 * Created by chien on 9/4/2017.
 */
public class CellLogicStep implements LogicStep<LogicBoard>
{
    int x;
    int y;

    public CellLogicStep(int x, int y) { this.x = x ; this.y = y;}

    @Override

    // so, the things that are legal:
    // 1 inbound, 1 outbound (i.e. 2 paths, 2 walls)
    // 2 crossing paths (4 paths, no walls)
    public LogicStatus apply(LogicBoard thing)
    {
        Vector<Direction> unknowns = new Vector<>();
        int pathcount = 0;
        int wallcount = 0;
        for (Direction d : Direction.orthogonals())
        {
            switch (thing.getEdge(x, y, d))
            {
                case PATH:
                    ++pathcount;
                    break;
                case NOTPATH:
                    ++wallcount;
                    break;
                case UNKNOWN:
                    unknowns.add(d);
            }
        }

        // cases:
        // x = can't happen
        // b = bad
        // C = good
        // ? = not enough info to do anything.
        // w = all unknowns go to walls
        // p = all unknowns go to paths
        //     pathcount
        // unk      0  1   2   3   4
        //      0   b  b   C   b   C
        //      1   b  p   w   p   x
        //      2   p  ?   ?   x   x
        //      3   ?  ?   x   x   x
        //      4   ?  x   x   x   x

        // row 0
        if (unknowns.size() == 0)
        {
            return (pathcount == 2 || pathcount == 4) ? LogicStatus.STYMIED : LogicStatus.CONTRADICTION;
        }
        // row 3 and 4
        if (unknowns.size() == 3 || unknowns.size() == 4) return LogicStatus.STYMIED;

        // only rows left are 1,2
        // this is the last 'bad' entry (row 1, col 0)
        if (pathcount == 0 && unknowns.size() ==1 ) return LogicStatus.CONTRADICTION;
        // last two not-enough-info on row 2
        if (unknowns.size() == 2 && pathcount > 0) return LogicStatus.STYMIED;
        // everything left is either a make-all-unknowns-walls or make-all-unknowns-paths
        EdgeType unknownsto = EdgeType.PATH;
        if (unknowns.size() == 1 && pathcount == 2) unknownsto = EdgeType.NOTPATH;

        for (Direction d : unknowns)
        {
            thing.setEdge(x,y,d,unknownsto);
        }
        return LogicStatus.LOGICED;
    }
}
