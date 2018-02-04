import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RegionLogicStep implements LogicStep<Board> {
    Collection<Point> points;
    public RegionLogicStep(Collection<Point> cellsOfRegion) { points = cellsOfRegion; }

    public LogicStatus apply(Board thing)
    {       List<Point> unknowns = new ArrayList<Point>();
        int blackcount = 0;
        int whitecount = 0;
        for (Point p : points) {
            switch (thing.getCell(p.x, p.y)) {
                case BLACK:
                    ++blackcount;
                    break;
                case WHITE:
                    ++whitecount;
                    break;
                case UNKNOWN:
                    unknowns.add(p);
                    break;
            }
        }
        if (blackcount > 0 && whitecount > 0) return LogicStatus.CONTRADICTION;
        if (unknowns.size() == 0) return LogicStatus.STYMIED;
        if (blackcount == 0 && whitecount == 0) return LogicStatus.STYMIED;

        if (blackcount > 0) for (Point p : unknowns) thing.setCell(p.x,p.y,CellType.BLACK);
        if (whitecount > 0) for (Point p : unknowns) thing.setCell(p.x,p.y,CellType.WHITE);
        return LogicStatus.LOGICED;

    }
}
