import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

public class CellLogicStep implements LogicStep<Board> {
    int x;
    int y;
    public CellLogicStep(int x, int y) { this.x = x; this.y = y; }

    @Override public LogicStatus apply(Board thing) {
        RectangleRegionSet rrs = thing.cells.getCell(x,y);
        if (rrs.size() == 0) return LogicStatus.CONTRADICTION;

        Set<Point> centers = new HashSet<>();
        for (RectangleRegion rr : rrs) centers.add(rr.realCenter);

        if (centers.size() > 1) return LogicStatus.STYMIED;

        LogicStatus result = LogicStatus.STYMIED;

        Point centerp = centers.iterator().next();
        RectangleRegionHandler rrh = thing.getHandler(centerp.x,centerp.y);

        if (rrh.realness == Realness.UNKNOWN) {
            rrh.realness = Realness.REAL;
            result = LogicStatus.LOGICED;
        }

        int origsize = rrh.currentRectangles.size();
        rrh.removeAllExcept(thing,rrs);
        int newsize = rrh.currentRectangles.size();
        if (newsize < origsize) result = LogicStatus.LOGICED;

        return result;
    }
}
