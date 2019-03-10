import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class BlackQuadLogicStep implements LogicStep<Board> {

    private static Direction[] dirs = new Direction[] { null, Direction.EAST, Direction.SOUTHEAST, Direction.SOUTH };
    private Point[] points = new Point[4];

    public BlackQuadLogicStep(int x, int y) {
        for (int i = 0 ; i < 4 ; ++i) {
            points[i] = dirs[i] == null ? new Point(x,y) : dirs[i].delta(points[0],1);
        }
    }

    @Override public LogicStatus apply(Board thing) {
        int numblack = 0;
        List<Point> unknowns = new ArrayList<>();

        for (Point p : points) {
            switch(thing.getCell(p.x,p.y)) {
                case WHITE: break;
                case BLACK: ++numblack; break;
                case UNKNOWN: unknowns.add(p); break;
            }
        }

        if (numblack == 4) return LogicStatus.CONTRADICTION;
        return LogicStatus.STYMIED;
    }
}
