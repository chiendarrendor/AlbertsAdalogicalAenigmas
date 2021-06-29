import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.awt.Point;

public class ArrowLogicStep implements LogicStep<Board> {
    int bx;
    int by;
    Direction d;
    int v;
    public ArrowLogicStep(int x, int y, Direction d, int v) { bx = x; by = y; this.d = d; this.v = v; }

    @Override public LogicStatus apply(Board thing) {
        LogicStatus result = LogicStatus.STYMIED;

        for(int dist = 1 ; ; ++dist) {
            Point p = d.delta(bx, by, dist);
            if (!thing.inBounds(p.x, p.y)) break;
            NumCell ns = thing.getCell(p.x, p.y);
            if (ns.isBroken()) return LogicStatus.CONTRADICTION;

            // if we get here, we are the closest possible cell to the clue that could have the number we desire.
            boolean canBeEmpty = false;
            boolean canBeNumber = false;

            for (int num = 0 ; num <= 5 ; ++num) {
                if (!ns.doesContain(num)) continue;

                if (num == 0) canBeEmpty = true;
                else if (num == v) canBeNumber = true;
                else {
                    ns.remove(num);
                    result = LogicStatus.LOGICED;
                }
            }
            if (ns.isBroken()) return LogicStatus.CONTRADICTION;
            if (canBeNumber) return result;
            // the only way we get here is if it can't be the number, but is not broken, therefore
            // is the blank.  we might have removed some numbers, that's in result, but we go on to the next cell
        }
        // the return inside the loop returns the first time we find a cell that could be the number we seek.
        // if we get here, that can only be because we failed to find a cell with a number.  That's bad.
        return LogicStatus.CONTRADICTION;
    }

    @Override public String toString() { return "ArrowLogicStep " + bx + "," + by; }
}
