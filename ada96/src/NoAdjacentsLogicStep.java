import grid.logic.LogicStatus;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class NoAdjacentsLogicStep extends ExactCellCountLogicStep {
    Point center;

    public NoAdjacentsLogicStep(Board b, int x, int y) {
        List<Point> cells = new ArrayList<>();
        center = new Point(x,y);
        for (int dy = -1 ; dy <= 1 ; ++dy) {
            for (int dx = -1 ; dx <= 1 ; ++dx) {
                if (dy == 0 && dx == 0) continue;
                if (!b.inBounds(x+dx,y+dy)) continue;
                cells.add(new Point(x+dx,y+dy));
            }
        }
        init(cells,0);
    }

    @Override public LogicStatus apply(Board thing) {
        CellState ccs = thing.getCell(center.x,center.y);
        if (ccs != CellState.MUST_HAVE_KNIGHT && ccs != CellState.POSITION_FINAL) return LogicStatus.STYMIED;

        return super.apply(thing);
    }
}
