import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class QuadLogicStep implements LogicStep<Board> {
    List<Point> cells = new ArrayList<>();
    public QuadLogicStep(int x, int y) {
        cells.add(new Point(x,y));
        cells.add(new Point(x+1,y));
        cells.add(new Point(x,y+1));
        cells.add(new Point(x+1,y+1));
    }

    @Override public LogicStatus apply(Board thing) {
        int pathcount = 0;
        int wallcount = 0;
        List<Point> unknowns = new ArrayList<>();
        for (Point p : cells) {
            switch(thing.getCell(p.x,p.y)) {
                case PATH: ++pathcount; break;
                case WALL: ++wallcount; break;
                case UNKNOWN: unknowns.add(p);
            }
        }

        if (pathcount > 3) return LogicStatus.CONTRADICTION;
        if (wallcount > 3) return LogicStatus.CONTRADICTION;
        if (unknowns.size() == 0) return LogicStatus.STYMIED;
        if (pathcount == 3) {
            unknowns.stream().forEach(p->thing.setCell(p.x,p.y,CellType.WALL));
            return LogicStatus.LOGICED;
        }
        if (wallcount == 3) {
            unknowns.stream().forEach(p->thing.setCell(p.x,p.y,CellType.PATH));
            return LogicStatus.LOGICED;
        }

        return LogicStatus.STYMIED;
    }
}
