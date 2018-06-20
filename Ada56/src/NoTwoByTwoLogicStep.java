import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.util.List;
import java.awt.Point;
import java.util.ArrayList;

public class NoTwoByTwoLogicStep implements LogicStep<Board> {
    List<Point> points = new ArrayList<>();
    public NoTwoByTwoLogicStep(int x, int y) {
        points.add(new Point(x,y));
        points.add(new Point(x+1,y));
        points.add(new Point(x,y+1));
        points.add(new Point(x+1,y+1));
    }

    List<Point> unknowns = new ArrayList<>();
    public LogicStatus apply(Board thing) {
        unknowns.clear();
        int blackcount = 0;
        int whitecount = 0;

        for (Point p : points) {
            switch(thing.getCell(p.x,p.y)) {
                case UNKNOWN: unknowns.add(p);  break;
                case WALL: ++blackcount; break;
                case PATH: ++whitecount; break;
            }
        }

        if (blackcount == 4) return LogicStatus.CONTRADICTION;
        if (whitecount == 4) return LogicStatus.CONTRADICTION;
        if (unknowns.size() == 0) return LogicStatus.STYMIED;

        if (whitecount == 3) {
            Point up = unknowns.get(0);
            thing.setCell(up.x,up.y,CellType.WALL);
            return LogicStatus.LOGICED;
        }

        if (blackcount == 3) {
            Point up = unknowns.get(0);
            thing.setCell(up.x,up.y,CellType.PATH);
            return LogicStatus.LOGICED;
        }


        return LogicStatus.STYMIED;
    }
}
