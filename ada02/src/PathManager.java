import grid.lambda.CellLambda;
import grid.puzzlebits.Direction;

import java.awt.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/**
 * Created by chien on 10/21/2017.
 */
public class PathManager
{
    class Path
    {
        Vector<Point> cells = new Vector<>();
        boolean isClosed = false;

        public Path() {}
        public Path(Path right)
        {
            cells.addAll(right.cells);
            isClosed = right.isClosed;
        }

        public boolean isTerminal(int x,int y)
        {
            if (x == cells.firstElement().x && y == cells.firstElement().y) return true;
            if (x == cells.lastElement().x && y == cells.lastElement().y) return true;
            return false;
        }

        public void canonicalizeStart(int x,int y)
        {
            if (x == cells.firstElement().x && y == cells.firstElement().y) return;
            Collections.reverse(cells);
        }


        public void canonicalizeEnd(int x, int y)
        {
            if(x == cells.lastElement().x && y == cells.lastElement().y) return;
            Collections.reverse(cells);
        }
    }


    class PathPointer
    {
        boolean isUnknown = true;
        Path pp;
    }

    int width;
    int height;
    Set<Path> paths = new HashSet<Path>();
    PathPointer[][] pathgrid;

    public PathManager(int width, int height)
    {
        this.width = width;
        this.height = height;
        pathgrid = new PathPointer[width][height];
        CellLambda.forEachCell(width,height,(x,y)->{
            pathgrid[x][y] = new PathPointer();
            pathgrid[x][y].pp = new Path();
            pathgrid[x][y].pp.cells.add(new Point(x,y));
            paths.add(pathgrid[x][y].pp);
        });
    }

    public PathManager(PathManager right)
    {
        this.width = right.width;
        this.height = right.height;
        pathgrid = new PathPointer[width][height];
        for(Path p : right.paths)
        {
            Path np = new Path(p);
            paths.add(np);
            for (Point pt : np.cells)
            {
                if (right.pathgrid[pt.x][pt.y] == null) continue;
                pathgrid[pt.x][pt.y] = new PathPointer();
                pathgrid[pt.x][pt.y].isUnknown = right.pathgrid[pt.x][pt.y].isUnknown;
                pathgrid[pt.x][pt.y].pp = np;
            }
        }
    }

    public PathState getPathState(int x, int y)
    {
        if (pathgrid[x][y] == null) return PathState.OFFPATH;
        return pathgrid[x][y].isUnknown ? PathState.UNKNOWN : PathState.ONPATH;
    }

    public void setPathState(int x, int y, PathState ps)
    {
        if (ps == PathState.OFFPATH)
        {
            Path p = pathgrid[x][y].pp;
            paths.remove(p);
            pathgrid[x][y] = null;
            return;
        }

        pathgrid[x][y].isUnknown = false;
    }

    public boolean isLoopPerfect()
    {
        // for loop to be perfect, the whole set of loops must be a single closed loop.
        if (paths.size() != 1) return false;
        Path thePath = paths.iterator().next();
        return thePath.isClosed;
    }

    public int numClosedLoops()
    {
        int result = 0;
        for (Path p : paths)
        {
            if (p.isClosed) ++result;
        }
        return result;
    }

    public int numPaths() { return paths.size(); }


    // joins x,y to x+d.dx,y+d.dy
    // returns false if
    // a) either cell is on a path, but not at the end
    // b) either cell is off path
    public boolean joinTo(int x, int y, Direction d)
    {
        int ox = x + d.DX();
        int oy = y + d.DY();

        if (pathgrid[x][y] == null) return false;
        if (pathgrid[ox][oy] == null) return false;
        Path p = pathgrid[x][y].pp;
        Path op = pathgrid[ox][oy].pp;
        if (!p.isTerminal(x,y)) return false;
        if (!op.isTerminal(ox,oy)) return false;
        if (p.isClosed) return false;
        if (op.isClosed) return false;

        // if we get here, we know both cells _can_ be on paths, and furthermore, there are not
        // already somewhere in the middle of one.  So, both cells _are_ on paths, no question.
        pathgrid[x][y].isUnknown = false;
        pathgrid[ox][oy].isUnknown = false;

        if(p == op)
        {
            p.isClosed = true;
            return true;
        }

        // so now we know that p and op are different.
        // we are going to be removing op and extending p.
        p.canonicalizeEnd(x,y);
        op.canonicalizeStart(ox,oy);
        for (Point pt : op.cells) { pathgrid[pt.x][pt.y].pp = p; }
        paths.remove(op);
        p.cells.addAll(op.cells);

        return true;
    }
}
