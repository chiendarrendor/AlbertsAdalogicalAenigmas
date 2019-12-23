import com.sun.org.apache.bcel.internal.generic.NOP;
import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import javax.management.relation.RoleUnresolved;
import java.awt.Point;
import java.util.Vector;

/**
 * Created by chien on 9/4/2017.
 */
public class CellLogicStep implements LogicStep<LogicBoard>
{
    int x;
    int y;
    boolean allowempty;

    public CellLogicStep(int x, int y,boolean allowempty) { this.x = x ; this.y = y; this.allowempty = allowempty; }

    private void apply(Vector<Direction> dirs, LogicBoard thing,EdgeType edge) { dirs.stream().forEach(d->thing.setEdge(x,y,d,edge));  }

    @Override

    // so, the things that are legal:
    // 1 inbound, 1 outbound (i.e. 2 paths, 2 walls)
    // 2 crossing paths (4 paths, no walls)
    // if allowempty is true...no paths at all
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

        // if empty cells are allowed:
        //      pathcount
        // unk      0  1   2   3   4
        //      0  C*  b   C   b   C
        //      1  w*  p   w   p   x
        //      2  ?*  ?   ?   x   x
        //      3  ?   ?   x   x   x
        //      4  ?   x   x   x   x

        if (pathcount == 0) {
            if (allowempty) {
                switch(unknowns.size()) {
                    case 0: return LogicStatus.STYMIED;
                    case 1: apply(unknowns,thing,EdgeType.NOTPATH); return LogicStatus.LOGICED;
                    default: return LogicStatus.STYMIED;
                }
            } else {
                switch(unknowns.size()) {
                    case 0:
                    case 1:
                        return LogicStatus.CONTRADICTION;
                    case 2:
                        apply(unknowns,thing,EdgeType.PATH); return LogicStatus.LOGICED;
                    default:
                        return LogicStatus.STYMIED;
                }
            }
        }

        switch(pathcount) {
            case 0: throw new RuntimeException("Shouldn't get here!");
            case 1:
                if (unknowns.size() == 0) return LogicStatus.CONTRADICTION;
                if (unknowns.size() == 1) {
                    apply(unknowns,thing,EdgeType.PATH);
                    return LogicStatus.LOGICED;
                }
                return LogicStatus.STYMIED;
            case 2:
                if (unknowns.size() == 1) {
                    apply(unknowns, thing, EdgeType.NOTPATH);
                    return LogicStatus.LOGICED;
                }
                return LogicStatus.STYMIED;
            case 3:
                if (unknowns.size() == 0) return LogicStatus.CONTRADICTION;
                apply(unknowns,thing,EdgeType.PATH); return LogicStatus.LOGICED;
            case 4:
                return LogicStatus.STYMIED;
            default: throw new RuntimeException("DEfINITELY shouldn't get here!");
        }
    }
}
