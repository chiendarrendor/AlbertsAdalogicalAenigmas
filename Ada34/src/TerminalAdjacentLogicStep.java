import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;
import grid.solverrecipes.singleloopflatten.EdgeState;

import java.awt.Point;

public class TerminalAdjacentLogicStep implements LogicStep<Board> {
    int x;
    int y;
    public TerminalAdjacentLogicStep(int x, int y) { this.x = x; this.y = y; }

    @Override public LogicStatus apply(Board thing) {
        CellType ct = thing.getCellType(x,y);
        if (ct != CellType.TERMINAL) return LogicStatus.STYMIED;

        Direction godir = null;
        for (Direction d : Direction.orthogonals()) {
            if (thing.getEdge(x,y,d) == EdgeState.PATH) {
                godir = d;
                break;
            }
        }

        if (godir == null) return LogicStatus.STYMIED;
        Point np = godir.delta(x,y,1);
        CellType nct = thing.getCellType(np.x,np.y);
        switch(nct) {
            case TERMINAL: return LogicStatus.CONTRADICTION;
            case STRAIGHT: return LogicStatus.STYMIED;
            case BEND: return LogicStatus.STYMIED;
            case NOTTERMINAL: return LogicStatus.STYMIED;
            case UNKNOWN:
                thing.setCellType(np.x,np.y,CellType.NOTTERMINAL);
                return LogicStatus.LOGICED;
            case NOTBEND:
                thing.setCellType(np.x,np.y,CellType.STRAIGHT);
                return LogicStatus.LOGICED;
            default:
                throw new RuntimeException("Can't get here!");
        }
    }
}
