import grid.logic.LogicStep;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

public abstract class CountingLogicStep implements LogicStep<Board> {
    private Set<Point> points = new HashSet<>();
    protected void addPoint(Point p) { points.add(p); }

    protected int shadedcount;
    protected int unshadedcount;
    protected Set<Point> unknowns = new HashSet<>();

    protected void count(Board thing) {
        shadedcount = 0;
        unshadedcount = 0;
        unknowns.clear();

        for (Point p: points) {
            switch(thing.getCell(p.x,p.y)) {
                case UNKNOWN: unknowns.add(p); break;
                case SHADED: ++shadedcount; break;
                case UNSHADED: ++unshadedcount; break;
            }
        }
    }
}
