import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class CellStateLogicStep implements LogicStep<Board> {
    int x;
    int y;
    public CellStateLogicStep(int x,int y) { this.x = x; this.y = y; }

    Set<Direction> walls = new HashSet<>();
    Set<Direction> paths = new HashSet<>();
    Set<Direction> unknowns = new HashSet<>();
    private void count(Board thing) {
        walls.clear();
        paths.clear();
        unknowns.clear();
        for (Direction d : Direction.orthogonals()) {
            switch(thing.getEdge(x,y,d)) {
                case PATH: paths.add(d); break;
                case WALL: walls.add(d); break;
                case UNKNOWN: unknowns.add(d); break;
            }
        }
    }

    @Override public LogicStatus apply(Board thing) {
        switch(thing.getCellType(x,y)) {
            case TERMINAL:
            case STRAIGHT:
            case BEND: return LogicStatus.STYMIED;

            case UNKNOWN: return applyUnknown(thing);
            case NOTBEND: return applyNotBend(thing);
            case NOTTERMINAL: return applyNotTerminal(thing);
            default: throw new RuntimeException("How did we get here?");
        }
    }

    //TERMINAL(true),
    //STRAIGHT(true),
    //BEND(true),

    //UNKNOWN(false), -> STRAIGHT,TERMINAL,BEND
    //NOTBEND(false), -> STRAIGHT, TERMINAL
    //NOTTERMINAL(false); -> STRAIGHT, BEND

    // only called if the state is not a final state
    private LogicStatus updateState(Board thing) {
        count(thing);
        CellType curstate = thing.getCellType(x,y);

        // is it a terminal?
        if (walls.size() == 3) {
            if (curstate == CellType.NOTTERMINAL) return LogicStatus.CONTRADICTION;
            thing.setCellType(x,y,CellType.TERMINAL);
            return LogicStatus.LOGICED;
        }

        // the other states (BEND,STRAIGHT) require two paths.
        if (paths.size() < 2) return LogicStatus.STYMIED;

        Iterator<Direction> dit = paths.iterator();
        Direction d1 = dit.next();
        Direction d2 = dit.next();

        CellType nextct = d1 == d2.getOpp() ? CellType.STRAIGHT : CellType.BEND;

        if (nextct == CellType.STRAIGHT) {
            thing.setCellType(x,y,nextct);
            return LogicStatus.LOGICED;
        }

        if (curstate == CellType.NOTBEND) return LogicStatus.CONTRADICTION;

        thing.setCellType(x,y,nextct);
        return LogicStatus.LOGICED;
    }


    private LogicStatus applyNotTerminal(Board thing) {
        LogicStatus result = LogicStatus.STYMIED;
        LogicStatus item = updateState(thing);
        if (item == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
        if (item == LogicStatus.LOGICED) result = LogicStatus.LOGICED;

        return result;
    }

    private LogicStatus applyUnknown(Board thing) {
        LogicStatus result = LogicStatus.STYMIED;
        LogicStatus item = updateState(thing);
        if (item == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
        if (item == LogicStatus.LOGICED) result = LogicStatus.LOGICED;

        return result;
    }

    private LogicStatus applyNotBend(Board thing) {
        LogicStatus result = LogicStatus.STYMIED;
        LogicStatus item = updateState(thing);
        if (item == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
        if (item == LogicStatus.LOGICED) result = LogicStatus.LOGICED;

        return result;
    }


}
