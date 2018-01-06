package grid.puzzlebits.Path;

import java.awt.*;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

public class Path implements Iterable<Point>
{
    private boolean isClosed = false;
    private Vector<Point> cells = new Vector<>();

    public Path(Point p1,Point p2) { cells.add(p1); cells.add(p2); }
    public Path(Path right) { cells.addAll(right.cells); isClosed = right.isClosed; }

    public Point endOne() { return cells.firstElement(); }
    public Point endTwo() { return cells.lastElement(); }
    public boolean isTerminal(Point p) { return p.equals(endOne()) || p.equals(endTwo()); }

    public void setClosed() { isClosed = true; }
    public boolean isClosed() { return isClosed; }

    public void reverse() { Collections.reverse(cells); }
    public Iterator<Point> iterator() { return cells.iterator(); }

    public void Merge(Path other, Point basis)
    {
        if (!isTerminal(basis) || !other.isTerminal(basis)) throw new RuntimeException("Paths not mergable");
        if (this == other)
        {
            isClosed = true;
            cells.remove(0);
            return;
        }

        if (!basis.equals(endTwo())) reverse();
        if (!basis.equals(other.endOne())) other.reverse();

        cells.remove(cells.size() - 1);
        cells.addAll(other.cells);
    }




}
