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
    public int size() { return cells.size(); }

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

    public class Cursor
    {
        int index;
        int origindex;
        public Cursor(int index) { this.index = index; this.origindex = index; }
        public boolean hasNext() { return isClosed() || index < cells.size()-1; }
        public boolean hasPrev() { return isClosed() || index > 0; }
        public void next()
        {
            if (!hasNext()) throw new RuntimeException("No next!");
            ++index;
            if (index >= cells.size()) index = 0;
        }
        public void prev()
        {
            if (!hasPrev()) throw new RuntimeException("no prev!");
            --index;
            if (index < 0) index = cells.size() - 1;
        }
        public void reset() { index = origindex; }
        public Point get() { return cells.elementAt(index);}
        public Point getPrev() { prev(); Point result = get(); next(); return result; }
        public Point getNext() { next(); Point result = get(); prev(); return result; }
    }

    public Cursor getCursor(int x,int y)
    {
        for (int i = 0 ; i < cells.size() ; ++i )
        {
            Point p = cells.elementAt(i);
            if (p.x == x && p.y == y) return new Cursor(i);
        }
        return null;
    }

}
