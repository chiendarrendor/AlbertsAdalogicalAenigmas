import java.awt.Point;
import java.util.Vector;

// assumes that the path in question is a loop.
public class PathCursor {
    int idx;
    Vector<Point> cells;
    int x;
    int y;
    public PathCursor(Path p,int x,int y) {
        cells = p.cells;
        this.x = x;
        this.y = y;

        for (idx = 0 ; idx < cells.size() ; ++idx) {
            if (cells.get(idx).x == x && cells.get(idx).y == y) break;
        }

        if (idx == cells.size()) throw new RuntimeException("PathCursor can't find " + x + "," + y);
    }

    private int nextidx() {
        if (idx+1 == cells.size()) return 0;
        return idx+1;
    }

    private int previdx() {
        if (idx == 0) return cells.size()-1;
        return idx-1;
    }

    public Point getCur() { return cells.get(idx); }
    public Point getNext() { return cells.get(nextidx()); }
    public Point getPrev() { return cells.get(previdx()); }
    public void next() { idx = nextidx(); }
    public void prev() { idx = previdx(); }
    public boolean atEnd() { return getCur().x == x && getCur().y == y; }
}
