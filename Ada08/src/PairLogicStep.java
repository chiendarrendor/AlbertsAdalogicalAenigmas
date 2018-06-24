import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class PairLogicStep implements LogicStep<Board> {
    Point[] pair = new Point[2];

    // Arrow should never be either of this pair.
    public PairLogicStep(int x1, int y1, int x2, int y2) {
        pair[0] = new Point(x1,y1);
        pair[1] = new Point(x2,y2);
    }

    List<Point> unknowns = new ArrayList<>();
    public LogicStatus apply(Board thing) {
        int numwall = 0;
        int numpath = 0;
        unknowns.clear();
        for (Point p : pair) {
            switch(thing.getCell(p.x,p.y)) {
                case UNKNOWN: unknowns.add(p); break;
                case PATH: ++numpath; break;
                case WALL: ++numwall; break;
            }
        }

        // P,W:  00 01 02 10 11 12 20 21 22
        //     P:
        // W 0 1 2
        // 0 B B B
        // 1     X
        // 2 A X X


        if (numwall > 1) return LogicStatus.CONTRADICTION; // A
        if (numwall == 0) return LogicStatus.STYMIED; // B
        if (numpath == 1) return LogicStatus.STYMIED;
        Point su = unknowns.get(0);
        thing.setCell(su.x,su.y,CellState.PATH);
        return LogicStatus.LOGICED;
    }
}
