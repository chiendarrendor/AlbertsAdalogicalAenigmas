import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

public class Region {
    char id;
    Set<Point> cells = new HashSet<>();
    public Region(char id) { this.id = id; }
    public void addCell(Point p) { cells.add(p); }
    public char getId() { return id; }
    public int size() { return cells.size(); }
}
