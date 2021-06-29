import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.awt.Point;

public class ZeroArrowLogicStep implements LogicStep<Board> {
    int x;
    int y;
    Direction d;
    public ZeroArrowLogicStep(int x, int y, Direction d) { this.x = x; this.y = y; this.d = d; }

    @Override public LogicStatus apply(Board thing) {
        LogicStatus result = LogicStatus.STYMIED;

        for(int dist = 1 ; ; ++dist) {
            Point p = d.delta(x,y,dist);
            if (!thing.inBounds(p.x,p.y)) break;
            NumCell ns = thing.getCell(p.x,p.y);
            if (ns.isBroken()) return LogicStatus.CONTRADICTION;
            if (!ns.doesContain(0)) return LogicStatus.CONTRADICTION;
            if (ns.isDone()) continue;
            ns.set(0);
            result = LogicStatus.LOGICED;
        }
        return result;
    }
}
