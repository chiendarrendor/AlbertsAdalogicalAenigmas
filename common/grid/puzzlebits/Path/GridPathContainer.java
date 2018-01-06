package grid.puzzlebits.Path;


// this class will allow the user to specify paths
// between cells of the grid (effectively, from one cell center to another)
// A path is a sequence of (not necessarily adjacent, nor unique) cells

// a path must contain at least two cells

// when the Merge() method is called, the user supplied lambda
// will be called on every Cell that has been updated by
// AddPath since the creation of the object or the last Merge()


import grid.lambda.CellLambda;

import java.awt.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

public class GridPathContainer implements Iterable<Path>
{
    public interface GPCMerger { public void op(int x, int y, GridPathCell gpc); }

    int width;
    int height;
    GridPathCell[][] cells;
    Set<Path> paths = new HashSet<>();
    Set<Point> dirtypoints = new HashSet<>();
    GPCMerger gpcm;


    public GridPathContainer(int width,int height,GPCMerger gpcm)
    {
        this.width = width;
        this.height = height;
        this.gpcm = gpcm;
        this.cells = new GridPathCell[width][height];
        CellLambda.forEachCell(width,height,(x,y)-> cells[x][y] = new GridPathCell(x,y,this));
    }

    public GridPathContainer(GridPathContainer right)
    {
        this.width = right.width;
        this.height = right.height;
        this.gpcm = right.gpcm;
        this.cells = new GridPathCell[width][height];
        CellLambda.forEachCell(width,height,(x,y)-> cells[x][y] = new GridPathCell(x,y,this));
        for (Path p : right.paths) { addPath(new Path(p)); }
        dirtypoints.addAll(right.dirtypoints);
    }

    public void addPath(Path p)
    {
        paths.add(p);
        for (Point pt : p)
        {
            if (pt.equals(p.endOne()) || pt.equals(p.endTwo()))
            {
                cells[pt.x][pt.y].addTerminalPath(p);
            }
            else
            {
                cells[pt.x][pt.y].addInternalPath(p);
            }
        }
    }

    public void removePath(Path p)
    {
        paths.remove(p);
        for (Point pt : p)
        {
            if (pt.equals(p.endOne()) || pt.equals(p.endTwo())) cells[pt.x][pt.y].removeTerminalPath(p);
            else cells[pt.x][pt.y].removeInternalPath(p);
        }
    }

    public void link(Point p1,Point p2)
    {
        Path p = new Path(p1,p2);
        addPath(p);
        dirtypoints.add(p1);
        dirtypoints.add(p2);
    }

    public void clean()
    {
        for (Point p : dirtypoints)
        {
            gpcm.op(p.x,p.y,cells[p.x][p.y]);
        }
        dirtypoints.clear();
    }

    @Override
    public Iterator<Path> iterator() { return paths.iterator(); }
}
