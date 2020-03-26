import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class CrateShift {
    private Point initial;
    private Point terminal;
    private List<Point> intermediates = new ArrayList<>();
    private int size;
    private Direction d = null;

    public Point getInitial() { return initial; }
    public Point getTerminal() { return terminal; }
    public List<Point> getIntermediates() { return intermediates; }
    public int getSize() { return size; }
    public Direction getDirection() { return d; }

    public CrateShift(Point stationary) {
        this.size = 0;
        this.initial = this.terminal = stationary;
    }

    public CrateShift(Point start,Point end) {
        this.initial = start;
        this.terminal = end;
        d = Direction.fromToOrthogonalNotAdjacent(start.x,start.y,end.x,end.y);
        for(int i = 1 ; ; ++i) {
            Point np = d.delta(start,i);
            if (np.equals(end)) break;
            intermediates.add(np);
        }
        size = intermediates.size() + 1;
    }
}
