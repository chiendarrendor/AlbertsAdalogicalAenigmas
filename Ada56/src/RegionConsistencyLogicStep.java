import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;


public class RegionConsistencyLogicStep implements LogicStep<Board> {

    List<Point> points = new ArrayList<>();

    List<Point> unknowns = new ArrayList<>();
    public LogicStatus apply(Board thing) {
        if (points.size() == 1) return LogicStatus.STYMIED;

        unknowns.clear();
        int wallcount = 0;
        int pathcount = 0;
        for (Point p : points) {
            switch (thing.getCell(p.x,p.y)) {
                case UNKNOWN: unknowns.add(p); break;
                case WALL: ++wallcount; break;
                case PATH: ++pathcount; break;
            }
        }

        if (wallcount > 0 && pathcount > 0) return LogicStatus.CONTRADICTION;
        if (unknowns.size() == 0) return LogicStatus.STYMIED;

        if (wallcount > 0) {
            for (Point p : unknowns) thing.setCell(p.x,p.y,CellType.WALL);
            return LogicStatus.LOGICED;
        }

        if (pathcount > 0) {
            for (Point p : unknowns) thing.setCell(p.x,p.y,CellType.PATH);
            return LogicStatus.LOGICED;
        }

        return LogicStatus.STYMIED;
    }

    public void addCell(Point point) {
        points.add(point);
    }
}
