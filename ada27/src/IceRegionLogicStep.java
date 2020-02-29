import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;
import grid.solverrecipes.singleloopflatten.EdgeState;

import java.awt.Point;
import java.util.Set;

public class IceRegionLogicStep implements LogicStep<Board> {
    Set<Point> ices;

    public IceRegionLogicStep(Set<Point> iceregion) { ices = iceregion; }

    @Override public LogicStatus apply(Board thing) {

        for(Point p : ices) {
            for (Direction d : Direction.orthogonals()) {
                if (thing.getEdge(p.x,p.y,d) != EdgeState.WALL) return LogicStatus.STYMIED;
            }
        }
        return LogicStatus.CONTRADICTION;
    }
}
