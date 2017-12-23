import grid.puzzlebits.Direction;

import java.awt.*;
import java.util.Collections;
import java.util.Vector;

/**
 * Created by chien on 9/4/2017.
 */
public class Path
{
    boolean isClosed = false;
    Vector<Point> cells = new Vector<>();

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



    public void reverse() { Collections.reverse(cells); }


    public void Merge(Direction dir, Path other,Point basis)
    {
        if (!endTwo().equals(basis) || endTwoDir() != dir) reverse();
        if (!other.endOne().equals(basis) ) other.reverse();

        if (!endTwo().equals(basis)  || !other.endOne().equals(basis)) throw new RuntimeException("mr?");

        // remove the duplicate point and merge
        cells.remove(cells.size()-1);
        cells.addAll(other.cells);
    }

    public Path(Point p1,Point p2)
    {
        cells.add(p1);
        cells.add(p2);
    }

    public Path(Path right) { cells.addAll(right.cells); isClosed = right.isClosed; }
}
