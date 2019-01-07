import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class AdjacentCellLogicStep implements LogicStep<Board> {
    int bid;
    int x;
    int y;
    List<Point> adjacents = new ArrayList<>();
    public AdjacentCellLogicStep(Board b, int bid, int x, int y) {
        this.bid = bid;
        this.x = x;
        this.y = y;

        Point cp = new Point(x,y);
        for (Direction d: Direction.values()) {
            Point ap = d.delta(cp,1);
            if (!b.inBounds(ap)) continue;
            adjacents.add(ap);
        }
    }

    @Override public LogicStatus apply(Board thing) {
        SubBoard sb = thing.getSubBoard(bid);
       if (sb.getCell(x,y) != CellState.BLACK) return LogicStatus.STYMIED;

        // if we get here, then our cell is black, and we must make sure that
        // no other adjacent cell is black.
        List<Point> unknowns = new ArrayList<>();

        for (Point adj : adjacents ) {
            switch(sb.getCell(adj.x,adj.y)) {
                case UNKNOWN: unknowns.add(adj); break;
                case BLACK: return LogicStatus.CONTRADICTION;
            }
        }

        // if we get here, then no adjacent cell was black, which is good.
        // let's make them all white.

        if (unknowns.size() == 0) return LogicStatus.STYMIED;
        unknowns.stream().forEach((p)->sb.setCell(p.x,p.y,CellState.WHITE));
        return LogicStatus.LOGICED;
    }
}
