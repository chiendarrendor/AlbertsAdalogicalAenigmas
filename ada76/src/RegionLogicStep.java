import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class RegionLogicStep implements LogicStep<Board> {
    private List<Point> cells;

    public RegionLogicStep(List<Point> cellsForRegion) { cells = cellsForRegion; }


    // a region cannot have more than one of a given shape
    // and _must_ have one of a given shape
    private LogicStatus applyShape(Board thing,CellShape cs) {
        Point theshape = null;
        List<Point> possibles = new ArrayList<>();

        for (Point p : cells) {
            Cell cell = thing.getCell(p.x,p.y);
            if (cell.isEmpty()) return LogicStatus.CONTRADICTION;
            if (!cell.hasPossible(cs)) continue;
            if (cell.isDone()) {
                if (theshape != null) return LogicStatus.CONTRADICTION;
                theshape = p;
            } else {
                possibles.add(p);
            }
        }

        LogicStatus result = LogicStatus.STYMIED;
        // if we get here, we know that we don't have two known copies of this shape.
        if (theshape != null) {
            // make all others impossible.
            for(Point p : possibles) {
                thing.getCell(p.x,p.y).remove(cs);
                result = LogicStatus.LOGICED;
            }
        } else {
            if (possibles.size() == 0) return LogicStatus.CONTRADICTION;
            if (possibles.size() > 1) return LogicStatus.STYMIED;
            // if we get here, we have no known, but a single possible...that's got to be it!
            Point mustbe = possibles.get(0);
            thing.getCell(mustbe.x,mustbe.y).set(cs);
            return LogicStatus.LOGICED;
        }


        return result;
    }


    @Override public LogicStatus apply(Board thing) {
        LogicStatus result = LogicStatus.STYMIED;

        LogicStatus tr = applyShape(thing,CellShape.TRIANGLE);
        if (tr == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
        if (tr == LogicStatus.LOGICED) result = LogicStatus.LOGICED;

        LogicStatus sr = applyShape(thing,CellShape.SQUARE);
        if (sr == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
        if (sr == LogicStatus.LOGICED) result = LogicStatus.LOGICED;

        LogicStatus cr = applyShape(thing,CellShape.CIRCLE);
        if (cr == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
        if (cr == LogicStatus.LOGICED) result = LogicStatus.LOGICED;

        return result;
    }
}
