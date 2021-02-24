
import grid.puzzlebits.Direction;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by chien on 9/4/2017.
 */
public class PathSet
{
    private class CellPointers
    {
        Path north;
        Path south;
        Path east;
        Path west;

        public void setDirection(Path p, Direction d)
        {
            switch(d)
            {
                case NORTH: north = p; break;
                case SOUTH: south = p; break;
                case EAST: east = p; break;
                case WEST: west = p; break;
            }
        }

        public Path getDirection(Direction d)
        {
            switch(d)
            {
                case NORTH: return north;
                case SOUTH: return south;
                case EAST: return east;
                case WEST: return west;
            }
            throw new RuntimeException("Unknown direction!");
        }

    }

    private Board b;
    private CellPointers[][] cells;
    Set<Path> paths = new HashSet<>();
    Set<Point> dirty = new HashSet<>();



    public PathSet(Board b)
    {
        this.b = b;
        cells = new CellPointers[b.getWidth()][b.getHeight()];
        b.forEachCell( (x,y) -> { cells[x][y] = new CellPointers(); return true; } );
    }

    public PathSet(Board b,PathSet right)
    {
        this(b);
        for (Path p : right.paths)
        {
            Path np = new Path(p);
            Point q = np.endOne();
            Point r = np.endTwo();
            cells[q.x][q.y].setDirection(np,np.endOneDir());
            cells[r.x][r.y].setDirection(np,np.endTwoDir());
            paths.add(np);
        }
        dirty.addAll(right.dirty);
    }

    public void AddPath(int x,int y, Direction d)
    {
        Point p1 = new Point(x,y);
        Point p2 = new Point(x+d.DX(),y+d.DY());

        Path p = new Path(p1,p2);
        cells[p1.x][p1.y].setDirection(p,d);
        cells[p2.x][p2.y].setDirection(p,d.getOpp());
        paths.add(p);
        dirty.add(p1);
        dirty.add(p2);
    }

    public void MergeAll()
    {
        Vector<Point> dirtyclear = new Vector<>();

        for (Point p : dirty)
        {
            CellPointers cp = cells[p.x][p.y];
            if (cp.north != null && cp.south != null) MergeOne(p,cp,Direction.NORTH,Direction.SOUTH);
            if (cp.east != null && cp.west != null) MergeOne(p,cp,Direction.EAST,Direction.WEST);

            // we can only merge two adjacents if wallcount is two
            Vector<Direction> dirs = new Vector<>();
            int wallcount = 0;
            for (Direction d : Direction.orthogonals())
            {
                if (cp.getDirection(d) != null) dirs.add(d);
                if (b.getEdge(p.x,p.y,d) == EdgeType.NOTPATH) ++wallcount;
            }

            if (dirs.size() == 2 && wallcount == 2)
            {
                MergeOne(p,cp,dirs.elementAt(0),dirs.elementAt(1));
            }


            if (cp.north == null && cp.east==null && cp.west == null && cp.south == null) dirtyclear.add(p);
        }
        for (Point p : dirtyclear) dirty.remove(p);
    }

    private void MergeOne(Point p,CellPointers cp, Direction d1, Direction d2)
    {
        Path p1 = cp.getDirection(d1);
        Path p2 = cp.getDirection(d2);

        cp.setDirection(null,d1);
        cp.setDirection(null,d2);
        if (p1 == p2)
        {
            p1.isClosed = true;
            return;
        }
        paths.remove(p2);
        p1.Merge(d1,d2,p2,p);
        cells[p1.endOne().x][p1.endOne().y].setDirection(p1,p1.endOneDir());
        cells[p1.endTwo().x][p1.endTwo().y].setDirection(p1,p1.endTwoDir());

//        showAreaPaths(p,8,0,12,4);

    }

    private class PathTuple
    {
        int x;
        int y;
        Direction dir;
        public PathTuple(int x,int y,Direction d) { this.x = x; this.y = y; this.dir = d; }
        public String toString() { return "(" + x + "," + y + "," + dir + ")"; }
    }



    public void validatePaths()
    {
        Map<Path,Vector<PathTuple>> mpi = new HashMap<>();
        b.forEachCell( (x,y) -> {
            for (Direction d : Direction.orthogonals())
            {
                if (cells[x][y].getDirection(d) == null) continue;
                if (!paths.contains(cells[x][y].getDirection(d)))
                {
                    System.out.println("x: " + x + " y: " + y + " d: " + d + " unknown path object!");
                    System.out.print("\t");
                    for (Point p : cells[x][y].getDirection(d).cells)
                    {
                        System.out.print("(" + p.x + "," + p.y + ")");
                    }
                    System.out.println("");
                }
                else
                {
                    if (!mpi.containsKey(cells[x][y].getDirection(d)))
                    {
                        mpi.put(cells[x][y].getDirection(d),new Vector<>());
                    }
                    mpi.get(cells[x][y].getDirection(d)).add(new PathTuple(x,y,d));
                }
            }
            return true;
        });

        for (Path p : paths)
        {
            Point e1 = p.endOne();
            Direction d1 = p.endOneDir();

            Point e2 = p.endTwo();
            Direction d2 = p.endTwoDir();

            if (!mpi.containsKey(p))
            {
                System.out.println("Strange...path " + p + " is not mentioned anywhere on the board...");
                continue;
            }
            Vector<PathTuple> vpt = mpi.get(p);
            int pcount = vpt.size();
            if (pcount < 2)
            {
                System.out.println("path " + p + " is not linked at both ends!");
                continue;
            }

            boolean plist = false;
            if (pcount > 2) { System.out.println("path " + p + " is linked other than at just ends"); plist = true; }

            if (cells[e1.x][e1.y].getDirection(d1) != p) { System.out.println("path " + p + " end 1 not linked to path"); plist = true; }
            if (cells[e2.x][e2.y].getDirection(d2) != p) { System.out.println("path " + p + " end 2 not linked to path"); plist = true; }

            if (plist)
            {

                System.out.print("\t");
                for (Point pp : p.cells) System.out.print("(" + pp.x + "," + pp.y + ")");
                System.out.println("");


                System.out.print("\t");
                for (PathTuple pt : vpt) System.out.print(pt);
                System.out.println("");
            }

        }
    }

    public void showAreaPaths(Point tp,int ulx,int uly, int lrx, int lry)
    {
        boolean found = false;

        Rectangle r = new Rectangle(ulx,uly,lrx-ulx,lry-uly);
        if (!r.contains(tp)) return;
        System.out.println("Operant; " + tp.x + "," + tp.y);

        for (Path p : paths)
        {
            if (!r.contains(p.endOne()) && !r.contains(p.endTwo())) continue;
            if (!found)
            {
                System.out.println("Paths in " + ulx + "<=x<=" + lrx + ", " + uly + "<=y<=" + lry);
                validatePaths();
                found = true;
            }
            for (Point pp : p.cells) System.out.print("(" + pp.x + "," + pp.y + ")");
            System.out.print((p.isClosed ? " CLOSED" : " OPEN"));
            System.out.println("");
        }
    }



}
