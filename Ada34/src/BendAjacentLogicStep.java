import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;
import grid.solverrecipes.singleloopflatten.EdgeState;

import java.awt.Point;

public class BendAjacentLogicStep implements LogicStep<Board> {
    int x;
    int y;
    public BendAjacentLogicStep(int x, int y) { this.x = x; this.y = y; }

    @Override public LogicStatus apply(Board thing) {
        LogicStatus result = LogicStatus.STYMIED;
        if (thing.getCellType(x,y) != CellType.BEND) return LogicStatus.STYMIED;

        for (Direction d : Direction.orthogonals()) {
            if (thing.getEdge(x,y,d) != EdgeState.PATH) continue;
            Point np = d.delta(x,y,1);

            switch(thing.getCellType(np.x,np.y)) {
                case TERMINAL:  break;
                case STRAIGHT: break;
                case BEND: return LogicStatus.CONTRADICTION;
                case NOTBEND: break;
                case UNKNOWN:
                    thing.setCellType(np.x,np.y,CellType.NOTBEND);
                    result = LogicStatus.LOGICED;
                    break;

                case NOTTERMINAL:
                    thing.setCellType(np.x,np.y,CellType.STRAIGHT);
                    result = LogicStatus.LOGICED;
                    break;
            }
        }
        return result;
    }
}
