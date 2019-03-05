package grid.puzzlebits;

import java.awt.*;
import java.util.Collection;
import java.util.Vector;

/**
 * Created by chien on 8/12/2017.
 */
// this class contains various functions on the concept of integer point adjacency
public class PointAdjacency
{
    public static boolean adjacent(Point p1,Point p2,boolean allowDiagonal)
    {
        for(Direction dir : allowDiagonal ? Direction.values() : Direction.orthogonals())
        {
            if (p1.x+dir.DX() == p2.x && p1.y+dir.DY() == p2.y) return true;
        }
        return false;
    }

    public static Direction adjacentDirection(Point p1,Point p2) {
        for (Direction dir: Direction.values()) {
            if (p1.x+dir.DX() == p2.x && p1.y+dir.DY() == p2.y) return dir;
        }
        return null;
    }


    public static boolean adjacentToAny(Point p1,Collection<Point> set,boolean allowDiagonal)
    {
        for (Point p : set) if (adjacent(p, p1, allowDiagonal)) return true;
        return false;
    }

    public static boolean allAdjacent(Collection<Point> points,boolean allowDiagonal)
    {
        if (points == null || points.size() < 2) return true;
        Vector<Point> result = new Vector<>();
        Vector<Point> current = new Vector<>();
        current.addAll(points);
        result.add(current.remove(0));

        while (current.size() > 0)
        {
            Vector<Point> newcurrent = new Vector<>();
            boolean acted = false;

            while (current.size() > 0)
            {
                Point cur = current.remove(0);
                if (adjacentToAny(cur, result, allowDiagonal))
                {
                    result.add(cur);
                    acted = true;
                }
                else
                {
                    newcurrent.add(cur);
                }
            }

            if (acted == false) return false;
            current = newcurrent;
        }
        return true;
    }
}
