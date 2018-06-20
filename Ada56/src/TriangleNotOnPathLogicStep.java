import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;

public class TriangleNotOnPathLogicStep implements LogicStep<Board> {
    Point p;

    public TriangleNotOnPathLogicStep(Point p) { this.p = p; }

    @Override
    public LogicStatus apply(Board thing) {
        return thing.isOnPath(p.x,p.y) ? LogicStatus.CONTRADICTION : LogicStatus.STYMIED;
    }
}
