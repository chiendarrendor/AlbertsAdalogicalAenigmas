import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.awt.Point;

public class CellPointsLogicStep implements LogicStep<Board> {
    int x;
    int y;
    public CellPointsLogicStep(int x, int y) { this.x = x; this.y = y; }

    @Override public LogicStatus apply(Board thing) {
        LogicStatus result = LogicStatus.STYMIED;
        Cell oc = thing.getCell(x,y);
        if (oc.size() == 0) return LogicStatus.CONTRADICTION;
        if (!oc.isArrow()) return LogicStatus.STYMIED;
        if (oc.size() > 1) return LogicStatus.STYMIED;
        Direction d = oc.getSolo();
        Direction od = d.getOpp();
        char orid = thing.getRegionId(x,y);

        // this will get set to the delta-index of the smallest delta-index that can possibly contain an arrow
        // back to us.
        int firstPossibleIndex = -1;
        int possibleArrowCount = 0;
        for (int idx = 1 ; ; ++idx) {
            Point tp = d.delta(x, y, idx);
            if (!thing.inBounds(tp)) break;
            Cell tc = thing.getCell(tp.x, tp.y);
            char trid = thing.getRegionId(tp.x, tp.y);

            boolean canBeArrow = !thing.regionsAdjacent(orid, trid) && tc.contains(od);
            boolean mustStop = tc.isArrow();

            if (canBeArrow) {
                ++possibleArrowCount;
                if (firstPossibleIndex == -1) {
                    firstPossibleIndex = idx;
                    // the very first spot an arrow can be must either be blank (we have to pass it) or back to us
                    // this is not true for later possibles (i.e. if the first arrow space is our arrow, we don't care what happens past that)
                    for (Direction remd : Direction.orthogonals()) {
                        if (remd == od) continue;
                        if (tc.contains(remd)) {
                            result = LogicStatus.LOGICED;
                            tc.clear(remd);
                        }
                    }
                }
            } else {
                // if this is not a spot we can have an arrow and we haven't see a spot for an arrow yet,
                // then this space must be blank.
                if (firstPossibleIndex == -1) {
                    if (!tc.contains(null)) return LogicStatus.CONTRADICTION;
                    if (tc.size() > 1) {
                        result = LogicStatus.LOGICED;
                        tc.set(null);
                    }
                }
            }

            if (mustStop) break;
        }

        if (possibleArrowCount == 0) return LogicStatus.CONTRADICTION;
        if (possibleArrowCount > 1) return result;

        // if we get here, we have only one possible place to put an arrow, and that is firstPossibleIndex
        // we must ensure that firstPossibleIndex is set to od
        // (we already know that all cells between our home and firstPossibleIndex are made empty)
        Point tp = d.delta(x,y,firstPossibleIndex);
        Cell tc = thing.getCell(tp.x,tp.y);
        if (tc.size() > 1) {
            result = LogicStatus.LOGICED;
            tc.set(od);
        }

        return result;
    }
}
