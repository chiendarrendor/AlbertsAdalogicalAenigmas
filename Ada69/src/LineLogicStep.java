import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class LineLogicStep implements LogicStep<Board> {
    List<Point> points = new ArrayList<>();
    CellState crossing;

    public LineLogicStep(int x, int y, int count, Direction d,CellState crossing) {
        this.crossing = crossing;
        Point curp = new Point(x,y);
        points.add(curp);

        while(points.size() < count) {
            curp = d.delta(curp,1);
            points.add(curp);
        }

    }

    private static CellState getcs(Board b,Point p) { return b.getCell(p.x,p.y); }


    @Override public LogicStatus apply(Board thing) {
        LogicStatus result = LogicStatus.STYMIED;

        for (int i = 0 ; i < points.size() - 1; ++i) {
            Point p1 = points.get(i);
            Point p2 = points.get(i+1);
            Point prev = i == 0 ? null : points.get(i-1);
            Point next = i+2 == points.size()  ? null : points.get(i+2);

            if (getcs(thing,p1) != crossing) continue;
            if (getcs(thing,p2) != crossing) continue;

            if (prev != null && getcs(thing,prev) == crossing) return LogicStatus.CONTRADICTION;
            if (next != null && getcs(thing,next) == crossing) return LogicStatus.CONTRADICTION;

            if (prev != null && getcs(thing,prev) == CellState.UNKNOWN) {
                result = LogicStatus.LOGICED;
                thing.setCell(prev.x,prev.y,crossing.getOpp());
            }

            if (next != null && getcs(thing,next) == CellState.UNKNOWN) {
                result = LogicStatus.LOGICED;
                thing.setCell(next.x,next.y,crossing.getOpp());
            }
        }


        return result;
    }
}
