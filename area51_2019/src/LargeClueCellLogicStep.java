import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

public class LargeClueCellLogicStep implements LogicStep<Board> {
    Set<Point> cells;
    public LargeClueCellLogicStep(LargeCell lc) { cells = lc.cells; }

    @Override public LogicStatus apply(Board thing) {
        int incount = 0;
        int outcount = 0;
        Set<Point> unknowns = new HashSet<>();

        for (Point p : cells) {
            switch(thing.getCellState(p.x,p.y)) {
                case UNKNOWN: unknowns.add(p); break;
                case OUTSIDE: ++outcount; break;
                case INSIDE: ++incount; break;
            }
        }
        if (incount == 0 && outcount == 0) return LogicStatus.STYMIED;
        if (incount > 0 && outcount > 0) return LogicStatus.CONTRADICTION;
        if (unknowns.size() == 0) return LogicStatus.STYMIED;
        CellState target = incount > 0 ? CellState.INSIDE : CellState.OUTSIDE;
        unknowns.stream().forEach(p->thing.setCellState(p.x,p.y,target));
        return LogicStatus.LOGICED;
    }
}
