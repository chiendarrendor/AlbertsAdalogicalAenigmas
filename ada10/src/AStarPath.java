import java.awt.Point;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

/**
 * Created by chien on 4/23/2017.
 */
public class AStarPath implements Comparable<AStarPath>
{
    Board b;
    private Set<Point> points = new HashSet<Point>();
     Vector<Point> path  = new Vector<Point>();

    public AStarPath(Board b)
    {
        this.b = b;
        addStep(new Point(0,0));
    }

    public AStarPath(AStarPath right)
    {
        b = right.b;
        for (Point p : right.path)
        {
            addStep(p);
        }
    }

    public int numberedRegionCount(Regions regions)
    {
        Set<Character> result = new HashSet<>();
        for (Point p : path)
        {
            char rid = b.getRegionId(p.x,p.y);
            Region r = regions.get(rid);
            if (!r.hasNumber()) continue;
            result.add(rid);
        }
        return result.size();
    }



    public void addStep(Point p)
    {
        points.add(p);
        path.add(p);
    }


    public int pathLen() { return path.size(); }

    public int grade()
    {
        Point tail = path.lastElement();
        return Math.abs(20-tail.x) + Math.abs(0-tail.y);
    }

    public List<AStarPath> successors()
    {
        Vector<AStarPath> result = new Vector<AStarPath>();
        Point tail = path.lastElement();
        List<Point> successors = b.adjacents(tail.x,tail.y);
        for (Point p : successors)
        {
            if (points.contains(p)) continue;
            if (b.getCell(p.x,p.y) == CellType.TREE) continue;
            AStarPath asp = new AStarPath(this);
            asp.addStep(p);
            result.add(asp);
        }
        return result;
    }

    @Override
    public int compareTo(AStarPath o)
    {
        int mysum = pathLen() + grade();
        int osum = o.pathLen() + o.grade();
        return Integer.compare(mysum,osum);
    }
}
