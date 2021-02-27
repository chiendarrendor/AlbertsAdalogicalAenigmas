import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SingleArrowRegionLogicStep implements LogicStep<Board> {
    Collection<Point> regionCells;
    public SingleArrowRegionLogicStep(Collection<Point> regionCells) { this.regionCells = regionCells; }

    @Override public LogicStatus apply(Board thing) {
        int arrowcount = 0;
        int blankcount = 0;
        List<Point> unknowns = new ArrayList<>();

        for(Point p : regionCells) {
            Cell c = thing.getCell(p.x,p.y);
            if (c.size() == 0) return LogicStatus.CONTRADICTION;

            if (c.isArrow()) {
                ++arrowcount;
            } else if (c.isBlank()) {
                ++blankcount;
            } else {
                unknowns.add(p);
            }
        }

        if (arrowcount > 1) return LogicStatus.CONTRADICTION;
        if (arrowcount + unknowns.size() < 1) return LogicStatus.CONTRADICTION;
        if (unknowns.size() == 0) return LogicStatus.STYMIED;

        LogicStatus result = LogicStatus.STYMIED;

        if (arrowcount == 1) {
            unknowns.stream().forEach(p->thing.getCell(p.x,p.y).set(null));
            result = LogicStatus.LOGICED;
        }

        if (arrowcount + unknowns.size() == 1) {
            unknowns.stream().forEach(p->thing.getCell(p.x,p.y).clear(null));
            result = LogicStatus.LOGICED;
        }

        return result;
    }
}
