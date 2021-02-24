import grid.puzzlebits.Direction;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * Created by chien on 9/4/2017.
 */
public class Path
{
    private static int nextPathId = 0;

    private int pathid;
    boolean isClosed = false;
    Vector<Point> cells = new Vector<>();
    List<String> logs = new ArrayList();

    Point endOne() { return cells.firstElement(); }
    Direction endOneDir()
    {
        // example:
        // if endOne is 2,3, and the next element in the list is 2,2, then we want NORTH
        Point ep = endOne();
        Point np = cells.elementAt(1);
        if (ep.y > np.y) return Direction.NORTH;
        if (ep.y < np.y) return Direction.SOUTH;
        if (ep.x > np.x) return Direction.WEST;
        return Direction.EAST;
    }

    Point endTwo() { return cells.lastElement(); }
    Direction endTwoDir()
    {
        Point ep = endTwo();
        Point np = cells.elementAt(cells.size() - 2);
        if (ep.y > np.y) return Direction.NORTH;
        if (ep.y < np.y) return Direction.SOUTH;
        if (ep.x > np.x) return Direction.WEST;
        return Direction.EAST;
    }



    public void reverse() { Collections.reverse(cells); logs.add("Reversing"); }


    public void Merge(Direction dir1,Direction dir2, Path other,Point basis)
    {
        if (!endTwo().equals(basis) || endTwoDir() != dir1) reverse();
        if (!endTwo().equals(basis) || endTwoDir() != dir1) throw new RuntimeException("Reversing Path this didn't help!");

        if (!other.endOne().equals(basis) || other.endOneDir() != dir2) other.reverse();
        if (!other.endOne().equals(basis) || other.endOneDir() != dir2) throw new RuntimeException("Reversing Path other didn't help!");

        StringBuffer sb = new StringBuffer();
        other.cells.stream().forEach(p->sb.append(" " + p));


        logs.add("Merging path " + other.pathid + ": " + sb.toString());

        // remove the duplicate point and merge
        cells.remove(cells.size()-1);
        cells.addAll(other.cells);

        StringBuffer sb2 = new StringBuffer();
        cells.stream().forEach(p->sb2.append(" " + p));
        logs.add("  Merged Path: " + sb2);
    }

    public Path(Point p1,Point p2)
    {
        cells.add(p1);
        cells.add(p2);
        pathid = ++nextPathId;
        logs.add("Path #" + pathid + " " + p1 + "->" + p2);
    }

    public Path(Path right) { cells.addAll(right.cells); isClosed = right.isClosed; pathid = right.pathid; logs.addAll(right.logs); }
    public int getPathId() { return pathid; }
}
