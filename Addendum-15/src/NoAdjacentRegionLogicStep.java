import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;
import java.util.Set;

public class NoAdjacentRegionLogicStep implements LogicStep<Board> {
    int regionid;
    public NoAdjacentRegionLogicStep(Region r) { regionid = r.getId(); }

    @Override public LogicStatus apply(Board thing) {
        Region r = thing.getRegionById(regionid);
        if (!r.isActive()) return LogicStatus.STYMIED;
        if (r.getActualSize() == -1) return LogicStatus.STYMIED;


        LogicStatus result = LogicStatus.STYMIED;

        Set<Point> adjset = r.getAdjacents(thing,false);
        for (Point p : adjset) {
            Region ar = thing.getRegionByCell(p.x, p.y);
            if (ar == null) continue;
            if (thing.canExtendInto(regionid, p)) continue;
            // we know r.getActualSize is not -1, so for this to be true means ar.getActualSize isn't either
            if (ar.getActualSize() == r.getActualSize()) return LogicStatus.CONTRADICTION;

            // if we get here, we can't extend into this point.  let's codify that.
            if (!r.hasEnemy(p)) {
                thing.addEnemy(regionid, p);
                result = LogicStatus.LOGICED;
            }

            // if we get here, then either ar.getActualSize is -1 or a positive value not r.getActualSize()
            if (ar.getActualSize() != -1) continue;

            if (ar.larger() == r.getActualSize()) {
                thing.setActualSize(ar.getId(), ar.smaller());
                result = LogicStatus.LOGICED;
            } else if (ar.smaller() == r.getActualSize()) {
                thing.setActualSize(ar.getId(), ar.larger());
                result = LogicStatus.LOGICED;
            }
        }

        return result;
    }
}
