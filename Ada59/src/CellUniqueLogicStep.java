import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CellUniqueLogicStep implements LogicStep<Board> {
    List<Point> points;
    public CellUniqueLogicStep(List<Point> points) {
        this.points = points;
    }

    @Override public LogicStatus apply(Board thing) {
        Set<Integer> byId = new HashSet<>();
        Set<Point> incompletes = new HashSet<>();

        for (Point p : points) {
            Cell c = thing.getCell(p.x,p.y);
            if (c.isBroken()) return LogicStatus.CONTRADICTION;
            if (c.isWall()) continue;
            if (!c.isComplete()) {
                incompletes.add(p);
                continue;
            }
            // if we get here, we know that this is a complete, non-wall cell.
            if (byId.contains(c.getSingleNumber())) return LogicStatus.CONTRADICTION;
            byId.add(c.getSingleNumber());
        }
        // If we get here, we know that every cell is unbroken, and if any cells are
        // complete, not-walls, that there are no duplicates.
        LogicStatus result = LogicStatus.STYMIED;
        for (int i : byId) {
            for (Point p : incompletes) {
                Cell c = thing.getCell(p.x,p.y);
                if (!c.contains(i)) continue;
                c.remove(i);
                result = LogicStatus.LOGICED;
                if (c.isBroken()) return LogicStatus.CONTRADICTION;
            }
        }

        return result;
    }
}
