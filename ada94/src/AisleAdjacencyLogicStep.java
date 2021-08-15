import grid.logic.LogicStatus;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class AisleAdjacencyLogicStep implements grid.logic.LogicStep<Board> {
    @Override public LogicStatus apply(Board thing) {
        LogicStatus result = LogicStatus.STYMIED;
        for (FutonPair pair : thing.getSetFutons()) {
            LogicStatus pairResult = doOnePair(thing,pair);
            if (pairResult == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
            if (pairResult == LogicStatus.LOGICED) result = LogicStatus.LOGICED;
        }

        return result;
    }

    private LogicStatus doOnePair(Board thing, FutonPair pair) {
        int aislecount = 0;
        int nonaislecount = 0;
        List<Point> unknowns = new ArrayList<>();

        for (Point p : pair.getAdjacents()) {
            Cell c = thing.getCell(p.x,p.y);
            if (!c.isValid()) return LogicStatus.CONTRADICTION;
            if (c.isDone()) {
                if (c.getDoneType() == CellType.AISLE) ++aislecount;
                else ++nonaislecount;
            } else {
                if (c.has(CellType.AISLE)) unknowns.add(p);
                else ++nonaislecount;
            }
        }

        if (aislecount > 0) return LogicStatus.STYMIED;
        // if we get here we have no known aisles
        if (unknowns.size() == 0) return LogicStatus.CONTRADICTION;
        // if we get here we have no known aisles, but we do have at least one unknown

        // if we have exactly one unknown (and no existing aisles), this unknown _must_ be an aisle
        if (unknowns.size() == 1) {
            unknowns.stream().forEach(p->thing.getCell(p.x,p.y).set(CellType.AISLE));
            return LogicStatus.LOGICED;
        }

        return LogicStatus.STYMIED;
    }
}
