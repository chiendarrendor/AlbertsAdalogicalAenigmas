import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CellBlockSomeOfLogicStep implements LogicStep<Board> {
    List<Point> points;
    int count;
    public CellBlockSomeOfLogicStep(int count, List<Point> points) { this.count = count; this.points = points; }

    public LogicStatus apply(Board thing)
    {
        List<Point> unknowns = new ArrayList<Point>();
        int blackweight = 0;
        int whiteweight = 0;
        int unkweight = 0;
        for (Point p : points) {
            switch (thing.getCell(p.x, p.y)) {
                case BLACK:
                    blackweight += thing.getWeight(p.x,p.y);
                    break;
                case WHITE:
                    whiteweight += thing.getWeight(p.x,p.y);
                    break;
                case UNKNOWN:
                    unknowns.add(p);
                    unkweight += thing.getWeight(p.x,p.y);
                    break;
            }
        }

        if (blackweight > count) return LogicStatus.CONTRADICTION;
        if (blackweight + unkweight < count) return LogicStatus.CONTRADICTION;
        if (unknowns.size() == 0) return LogicStatus.STYMIED;

        if (blackweight == count)
        {
            for (Point p : unknowns) thing.setCell(p.x,p.y,CellType.WHITE);
            return LogicStatus.LOGICED;
        }

        if (blackweight + unkweight == count)
        {
            for (Point p : unknowns) thing.setCell(p.x,p.y,CellType.BLACK);
            return LogicStatus.LOGICED;
        }

        return LogicStatus.STYMIED;
    }
}
