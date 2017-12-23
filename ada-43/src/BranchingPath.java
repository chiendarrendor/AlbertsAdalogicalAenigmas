import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.List;

/**
 * Created by chien on 5/6/2017.
 */
public class BranchingPath
{
    private BranchingPath parent = null;
    Vector<Point> myPath = new Vector<>();
    Set<Point> points = new HashSet<Point>();

    public List<Point> getPath()
    {
        Vector<Point> result = new Vector<>();
        if (parent != null) result.addAll(parent.getPath());
        result.addAll(myPath);
        return result;
    }

    public boolean hasPoint(Point p)
    {
        if (points.contains(p)) return true;
        if (parent == null) return false;
        return parent.hasPoint(p);
    }

    public int length()
    {
        int result = 0;
        if (parent != null) result += parent.length();
        result += myPath.size();
        return result;
    }

    public int depth()
    {
        if (parent == null) return 1;
        else return 1 + parent.depth();
    }

    public BranchingPath(Point p)
    {
        this(null,p);
    }

    public BranchingPath(BranchingPath parent,Point p)
    {
        this.parent = parent;
        AddPoint(p);
    }

    public void AddPoint(Point p)
    {
        myPath.add(p);
        points.add(p);
    }

    // returns:
    // null if this path is terminal
    // otherwise list of potentially extendible paths
    List<BranchingPath> walk(Board b)
    {
        Point terminal = myPath.lastElement();
        if (b.getCell(terminal.x,terminal.y) != CellType.BRICK) throw new RuntimeException("Path not on brick!");

        Vector<Point> extenders = new Vector<>();
        for (Point p : b.getAdjacents(terminal.x,terminal.y,true) )
        {
            if (b.getCell(p.x,p.y) != CellType.BRICK) continue;
            if (hasPoint(p)) continue;
            extenders.add(p);
        }
        if (extenders.size() == 0) return null;

        Vector<BranchingPath> result = new Vector<>();

        if (extenders.size() == 1)
        {
            AddPoint(extenders.firstElement());
            result.add(this);
        }
        else
        {
            for(Point p: extenders)
            {
                result.add(new BranchingPath(this,p));
            }
        }
        return result;
    }

    public void walkPath(Board b)
    {
        Set<Point> seen = new HashSet<Point>();
        List<Point> path = getPath();
        for (int i = 0 ; i < path.size() - 1 ; ++i)
        {
            Point curp = path.get(i);
            Point nexp = path.get(i+1);
            int dx = nexp.x - curp.x;
            int dy = nexp.y - curp.y;
            if (dx == 0 || dy == 0) continue;
            if (dx == 1 && dy == 1)
            {
                Point lp = new Point(curp.x+1,curp.y);
                if (!seen.contains(lp))
                {
                    seen.add(lp);
                    System.out.print(b.getLetter(lp.x,lp.y));
                }
                Point rp = new Point(curp.x,curp.y+1);
                if (!seen.contains(rp))
                {
                    seen.add(rp);
                    System.out.print(b.getLetter(rp.x,rp.y));
                }
            }
            else if(dx == -1 && dy == -1)
            {
                Point lp = new Point(curp.x-1,curp.y);
                if (!seen.contains(lp))
                {
                    seen.add(lp);
                    System.out.print(b.getLetter(lp.x,lp.y));
                }
                Point rp = new Point(curp.x,curp.y-1);
                if (!seen.contains(rp))
                {
                    seen.add(rp);
                    System.out.print(b.getLetter(rp.x,rp.y));
                }
            }
            else if (dx == -1 && dy == 1)
            {
                Point lp = new Point(curp.x,curp.y+1);
                if (!seen.contains(lp))
                {
                    seen.add(lp);
                    System.out.print(b.getLetter(lp.x,lp.y));
                }
                Point rp = new Point(curp.x-1,curp.y);
                if (!seen.contains(rp))
                {
                    seen.add(rp);
                    System.out.print(b.getLetter(rp.x,rp.y));
                }
            }
            else if (dx == 1 && dy == -1)
            {
                Point lp = new Point(curp.x,curp.y-1);
                if (!seen.contains(lp))
                {
                    seen.add(lp);
                    System.out.print(b.getLetter(lp.x,lp.y));
                }
                Point rp = new Point(curp.x+1,curp.y);
                if (!seen.contains(rp))
                {
                    seen.add(rp);
                    System.out.print(b.getLetter(rp.x,rp.y));
                }
            }
            else
            {
                throw new RuntimeException("Unknown Delta!" + dx + " " + dy);
            }
        }

        System.out.println("");

    }


}
