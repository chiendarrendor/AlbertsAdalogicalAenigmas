import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

public class RegionLogicStep implements LogicStep<Board> {
    int x;
    int y;

    public RegionLogicStep(int x, int y) { this.x = x; this.y = y; }

    @Override public LogicStatus apply(Board thing) {
        RectangleRegionHandler rrh = thing.getHandler(x,y);

        if (rrh.currentRectangles.size() == 0) {
            switch(rrh.realness) {
                case REAL: return LogicStatus.CONTRADICTION;
                case FAKE: return LogicStatus.STYMIED;
                case UNKNOWN: rrh.realness = Realness.FAKE; return LogicStatus.LOGICED;
            }
        }
        // if we get here we have at least one rectangle.

        if (rrh.realness == Realness.FAKE) {
            rrh.removeAll(thing);
            return LogicStatus.LOGICED;
        }
        // if we get here, we have at least one rectangle, and we're not fake.
        if (rrh.realness == Realness.UNKNOWN) return LogicStatus.STYMIED;

        // if we get here, we're real and have the right to push aside other rectangles.
        // we start by calculating the intersection of all real points of all rectangles in us.
        // we are guaranteed that this intersection is at least of size 1.
        Set<Point> intersection = null;
        for(RectangleRegion rr : rrh.currentRectangles) {
            if (intersection == null) {
                intersection = new HashSet<>();
                intersection.addAll(rr.realPoints);
            } else {
                intersection.retainAll(rr.realPoints);
            }
        }

        LogicStatus result = LogicStatus.STYMIED;
        for (Point p : intersection) {
            RectangleRegionSet rrs = thing.cells.getCell(p.x,p.y);
            Set<RectangleRegion> temp = new HashSet<RectangleRegion>(rrs);
            for (RectangleRegion t : temp) {
                if (!rrh.currentRectangles.contains(t)) {
                    thing.removeRectangle(t);
                    result = LogicStatus.LOGICED;
                }
            }
        }

        return result;
    }
}
