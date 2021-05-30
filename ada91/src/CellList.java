import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CellList {
    private List<Point> cells = new ArrayList<>();
    private List<Point> reversed = new ArrayList<>();
    public CellList(int sx,int sy,int count, Direction d) {
        for (int i = 0 ; i < count ; ++i) {
            Point p = d.delta(sx,sy,i);
            cells.add(p);
            reversed.add(p);
        }
        Collections.reverse(reversed);
    }

    public List<Point> cells() { return cells; }
    public List<Point> reversed() { return reversed; }
    public Point start() { return cells.get(0); }
    public Point end() { return cells.get(cells.size()-1); }
}
