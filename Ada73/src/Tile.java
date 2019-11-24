import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

public class Tile {
    Set<Point> cells = new HashSet<>();
    String name;
    public Tile(Point base,int size, Direction d) {
        for (int i = 0 ; i < size ; ++i) { cells.add(d.delta(base,i)); }
        name = "Tile (" + base.x + "," + base.y + ") -> " + d + " (" + size + ")";
    }
    public int size() { return cells.size(); }
    public String toString() { return name; }
}
