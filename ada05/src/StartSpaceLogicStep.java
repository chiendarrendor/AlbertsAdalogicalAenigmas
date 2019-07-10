import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;
import grid.solverrecipes.singleloopflatten.EdgeState;

import java.awt.Point;

public class StartSpaceLogicStep implements LogicStep<Board> {
    Point startcell;
    public StartSpaceLogicStep(Point startCell) { this.startcell = startCell; }

    @Override public LogicStatus apply(Board thing) {
        int wallcount = 0;
        for (Direction d: Direction.orthogonals()) {
            if (thing.getEdge(startcell.x,startcell.y,d) == EdgeState.WALL) {
                ++wallcount;
                if (wallcount > 2) return LogicStatus.CONTRADICTION;
            }

        }
        return LogicStatus.STYMIED;
    }
}
