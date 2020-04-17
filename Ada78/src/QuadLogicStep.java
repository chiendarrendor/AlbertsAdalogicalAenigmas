import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class QuadLogicStep implements LogicStep<Board> {
    int x;
    int y;
    public QuadLogicStep(int x, int y) { this.x = x; this.y = y; }

    @Override public LogicStatus apply(Board thing) {
        int labelcount = 0;
        List<Point> unknowns = new ArrayList<>();
        for (int dx = 0 ; dx < 2 ; ++dx) {
            for (int dy = 0 ; dy < 2 ; ++dy) {
                switch (thing.getCell(x+dx,y+dy)) {
                    case BARRIER: break;
                    case LABEL: ++labelcount; break;
                    case UNKNOWN: unknowns.add(new Point(x+dx,y+dy));
                }
            }
        }

        if (labelcount == 4) return LogicStatus.CONTRADICTION;
        if (labelcount < 3) return LogicStatus.STYMIED;
        if (unknowns.size() == 0) return LogicStatus.STYMIED;
        unknowns.stream().forEach(p->thing.setCell(p.x,p.y,CellState.BARRIER));
        return LogicStatus.LOGICED;
    }
}
