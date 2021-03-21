import grid.graph.GridGraph;

import java.awt.Point;
import java.util.Set;

class MyGridReference implements GridGraph.GridReference {
    Set<Point> cells;
    Point ignore = null;
    public MyGridReference(Set<Point>cells) { this.cells = cells; }
    public MyGridReference(Set<Point>cells, Point ignore) { this.cells = cells; this.ignore = ignore; }

    @Override public int getWidth() { return cells.stream().mapToInt(p->p.x).max().getAsInt() + 1; }
    @Override public int getHeight() { return cells.stream().mapToInt(p->p.y).max().getAsInt() + 1; }
    @Override public boolean isIncludedCell(int x, int y) {
        if (ignore != null && ignore.x == x && ignore.y == y) return false;
        return cells.contains(new Point(x,y));
    }
    @Override public boolean edgeExitsEast(int x, int y) { return true; }
    @Override public boolean edgeExitsSouth(int x, int y) { return true; }
}
