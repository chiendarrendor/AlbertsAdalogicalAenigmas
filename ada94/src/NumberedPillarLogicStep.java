import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class NumberedPillarLogicStep implements LogicStep<Board> {
    List<Point> adjacents = new ArrayList<>();
    int value;
    public NumberedPillarLogicStep(Board b, int x, int y, int numericPillarValue) {
        for (Direction d: Direction.orthogonals()) {
            Point p = d.delta(x,y,1);
            if (!b.inBounds(p.x,p.y)) continue;
            if (b.hasPillar(p.x,p.y)) continue;
            adjacents.add(p);
        }
        value = numericPillarValue;
    }

    @Override public LogicStatus apply(Board thing) {
        int pillowcount = 0;
        int nonpillowcount = 0;
        List<Point> unknowns = new ArrayList<>();

        for (Point adj : adjacents) {
            Cell c = thing.getCell(adj.x,adj.y);
            if (!c.isValid()) return LogicStatus.CONTRADICTION;
            if (c.isDone()) {
                if (c.getDoneType() == CellType.PILLOW) ++pillowcount;
                else ++nonpillowcount;
            } else {
                if (c.has(CellType.PILLOW)) unknowns.add(adj);
                else ++nonpillowcount;
            }
        }

        if (pillowcount > value) return LogicStatus.CONTRADICTION;
        if (pillowcount + unknowns.size() < value) return LogicStatus.CONTRADICTION;
        if (unknowns.size() == 0) return LogicStatus.STYMIED;
        if (pillowcount == value) {
            unknowns.stream().forEach(p->thing.getCell(p.x,p.y).clear(CellType.PILLOW));
            return LogicStatus.LOGICED;
        }
        if (pillowcount + unknowns.size() == value) {
            unknowns.stream().forEach(p->thing.getCell(p.x,p.y).set(CellType.PILLOW));
            return LogicStatus.LOGICED;
        }

        return  LogicStatus.STYMIED;
    }
}
