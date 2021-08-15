import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class TwoByTwoAisleLogicStep implements LogicStep<Board> {
    private List<Point> cells = new ArrayList<>();
    public TwoByTwoAisleLogicStep(int x, int y) {
        cells.add(new Point(x,y));
        cells.add(new Point(x+1,y));
        cells.add(new Point(x,y+1));
        cells.add(new Point(x+1,y+1));
    }

    @Override public LogicStatus apply(Board thing) {
        int aislecount = 0;
        int nonaislecount = 0;
        List<Point> unknowns = new ArrayList<>();

        for (Point p : cells) {
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

        if (aislecount == 4) return LogicStatus.CONTRADICTION;
        if (nonaislecount > 0) return LogicStatus.STYMIED;
        if (unknowns.size() == 0) return LogicStatus.STYMIED;
        if (aislecount < 3) return LogicStatus.STYMIED;
        // if we get here, we have 3 aisles and (exactly) 1 unknown.

        unknowns.stream().forEach(p->thing.getCell(p.x,p.y).clear(CellType.AISLE));
        return LogicStatus.LOGICED;
    }
}
