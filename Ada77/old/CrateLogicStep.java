import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;
import java.util.List;

public class CrateLogicStep implements LogicStep<Board> {
    Point me;

    public CrateLogicStep(int x, int y) { me = new Point(x,y); }

    @Override public LogicStatus apply(Board thing) {
        if (thing.hasMoved(me)) return LogicStatus.STYMIED;
        List<Point> destinations = thing.canGoFromHere(me.x,me.y,true);
        if (destinations.size() == 0) return LogicStatus.CONTRADICTION;
        if (destinations.size() > 1) return LogicStatus.STYMIED;

        thing.doMove(me,destinations.get(0));
        return LogicStatus.LOGICED;
    }
}
