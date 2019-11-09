import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;
import grid.solverrecipes.singleloopflatten.EdgeState;

import java.awt.Point;

public class ArrowClueLogicStep implements LogicStep<Board> {
    int x;
    int y;
    Direction d;
    int dx;
    int dy;
    public ArrowClueLogicStep(int x, int y, char arrowClue) {
        this.x = x;
        this.y = y;
        switch(arrowClue) {
            case '<': d = Direction.WEST; break;
            case '>': d = Direction.EAST; break;
            case 'v': d = Direction.SOUTH; break;
            case '^': d = Direction.NORTH; break;
            default: throw new RuntimeException("Unknown arrow direction!");
        }
        Point p = d.delta(x,y,1);
        dx = p.x;
        dy = p.y;
    }

    @Override public LogicStatus apply(Board thing) {
        LogicStatus result = LogicStatus.STYMIED;

        if (thing.getCellType(x,y) == CellType.UNKNOWN) {
            thing.setCellType(x,y,CellType.TERMINAL);
            result = LogicStatus.LOGICED;
        } else if (thing.getCellType(x,y) != CellType.TERMINAL) {
            return LogicStatus.CONTRADICTION;
        }

        if (thing.getEdge(x,y,d) == EdgeState.UNKNOWN) {
            thing.setEdge(x,y,d,EdgeState.PATH);
            result = LogicStatus.LOGICED;
        } else if (thing.getEdge(x,y,d) != EdgeState.PATH) {
            return LogicStatus.CONTRADICTION;
        }

        return result;
    }
}
