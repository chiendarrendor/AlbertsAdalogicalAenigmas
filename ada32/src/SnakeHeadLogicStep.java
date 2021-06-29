import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.awt.Point;

public class SnakeHeadLogicStep implements LogicStep<Board> {
    int x;
    int y;

    public SnakeHeadLogicStep(int x, int y) { this.x = x; this.y = y; }

    @Override public LogicStatus apply(Board thing) {
        NumCell ns = thing.getCell(x,y);

        if (ns.isBroken()) return LogicStatus.CONTRADICTION;
        if (!ns.isDone()) return LogicStatus.STYMIED;
        if (ns.getComplete() != 2) return LogicStatus.STYMIED;

        // if we get here, we're standing on a '2' cell.

        Direction oned = null;
        Point onep = null;

        for (Direction d : Direction.orthogonals()) {
            Point tp = d.delta(x,y,1);
            if (!thing.inBounds(tp.x,tp.y)) continue;
            NumCell tns = thing.getCell(tp.x,tp.y);
            if (tns.isBroken()) return LogicStatus.CONTRADICTION;
            if (!tns.isDone()) continue;
            if (tns.getComplete() != 1) continue;

            if (onep != null) return LogicStatus.CONTRADICTION;
            oned = d;
            onep = tp;
        }

        if (onep == null) return LogicStatus.STYMIED;

        // if we get here, we are a 2 and we are next to a 1, which means that all cells out in that
        // direction must be empty.

        LogicStatus result = LogicStatus.STYMIED;

        for (int idx = 1 ; ; ++idx){
            Point np = oned.delta(onep,idx);
            if (!thing.inBounds(np.x,np.y)) break;
            NumCell dns = thing.getCell(np.x,np.y);
            if (dns.isBroken()) return LogicStatus.CONTRADICTION;
            if (!dns.doesContain(0)) return LogicStatus.CONTRADICTION;
            if (dns.isDone()) continue;
            dns.set(0);
            result = LogicStatus.LOGICED;
        }

        return result;
    }
}
