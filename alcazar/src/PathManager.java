import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chien on 7/24/2017.
 */
public class PathManager
{
    int width;
    int height;
    Board.CellInfo outside;
    boolean isLive = true;


    static int nextid = 0;
    public class Path
    {
        Board.CellInfo end1;
        Board.CellInfo end2;
        boolean valid = true;
        boolean isLoop = false;
        int id;

        public String toString()
        {
            StringBuffer sb = new StringBuffer();
            sb.append("id: ").append(id);
            sb.append(" valid: ").append(valid);
            sb.append(" isLoop: ").append(isLoop);
            sb.append(" ");
            if (end1 == outside)
            {
                sb.append("outside");
            }
            else
            {
                sb.append("(").append(end1.x).append(",").append(end1.y).append(")");
            }
            sb.append("->");
            if (end2 == outside)
            {
                sb.append("outside");
            }
            else
            {
                sb.append("(").append(end2.x).append(",").append(end2.y).append(")");
            }
            return sb.toString();
        }


        public Path(Board.CellInfo ci) { end1 = ci; end2 = ci; id = ++nextid; }
        public Path(Path right)
        {
            end1 = right.end1 ;
            end2 = right.end2 ;
            valid = right.valid ;
            id = right.id;
            isLoop = right.isLoop;
        }
        public void invalidate() { valid = false; }

        public boolean terminatesHere(Board.CellInfo ci)
        {
            return end1 == ci || end2 == ci;
        }


        // this will cause end1 to be c1, possibly causing a swap.
        // if _neither_ end is c1, throw an exception; we did something naughty.
        public void canonicalize(Board.CellInfo ci,Board b)
        {
            if (ci == end1)
            {
                // do nothing...everything is fine.
            }
            else if (ci == end2)
            {
                end2 = end1;
                end1 = ci;
            }
            else
            {
                throw new AlcazarRuntimeException("can't canonicalize if neither end! " + ci.x + " " + ci.y,b);
            }
        }
    }

    Map<Integer,Path> pathsbyid = new HashMap<>();
    Path[][] pathsongrid;

    public Collection<Path> getAllActivePaths()
    {
        return pathsbyid.values();
    }


    public PathManager(Board b)
    {
        width = b.getWidth();
        height = b.getHeight();
        outside = b.outside;

        pathsongrid = new Path[width][height];
        for (int x = 0 ; x < width ; ++x)
        {
            for (int y = 0 ; y < height ; ++y)
            {
                pathsongrid[x][y] = new Path(b.getCI(x,y));
                pathsbyid.put(pathsongrid[x][y].id,pathsongrid[x][y]);
            }
        }
    }

    public PathManager(PathManager right)
    {
        width = right.width;
        height = right.height;
        outside = right.outside;

        pathsongrid = new Path[width][height];
        for (int x = 0 ; x < width ; ++x)
        {
            for (int y = 0; y < height; ++y)
            {
                Path curp = right.pathsongrid[x][y];
                if (!curp.valid)
                {
                    // if the path isn't valid, processing it will throw an exception anyway, so it doesn't matter
                    // if we make duplicates.
                    pathsongrid[x][y] = new Path(curp);
                    continue;
                }
                // if we get here, we have to deal with a valid path.
                Path newp = null;

                if (!pathsbyid.containsKey(curp.id))
                {
                    newp = new Path(curp);
                    pathsbyid.put(newp.id, newp);
                }
                else
                {
                    newp = pathsbyid.get(curp.id);
                }
                pathsongrid[x][y] = newp;
            }
        }
    }

    public void Terminate(Board.CellInfo c1)
    {
        if (c1.isOutside) throw new RuntimeException("Can't terminate outside!");
        Path p1 = pathsongrid[c1.x][c1.y];
        if (!p1.valid) throw new RuntimeException("Can't terminate invalid!");
        p1.canonicalize(c1,null);
        p1.end1 = outside;
    }



    public void Merge(Board.CellInfo c1,Board.CellInfo c2,Board b)
    {
        if (c1.isOutside || c2.isOutside) throw new RuntimeException("Can't merge to outside, must terminate");

        Path p1 = pathsongrid[c1.x][c1.y];
        Path p2 = pathsongrid[c2.x][c2.y];

        // another special case...if the grid contains an invalid path due to that cell being in a longer path,
        // this is still possible...there may not be a valid path to mark as a loop, so we kill the whole PathManager
        if (!p1.valid || !p2.valid)
        {
            isLive = false;
            return;
        }

        // this means that the two cells are ends of the same path, and merging would be a contradiction.
        // we still need to process it and remember that it is a contradiction
        if (p1.id == p2.id)
        {
            p1.end1 = p1.end2 = c1;
            p1.isLoop = true;
            return;
        }

        // special case...this might happen before we realize that a cell in the middle of a path can't
        // be entered....mark the offending path as a loop, because that will eventually terminate the
        // board as invalid.
        if (!p1.terminatesHere(c1))
        {
            p1.isLoop = true;
            return;
        }

        if (!p2.terminatesHere(c2))
        {
            p2.isLoop = true;
            return;
        }


        // this makes the given CellInfo end1 of our paths, reducing the number of cases to code.
        p1.canonicalize(c1,b);
        p2.canonicalize(c2,b);

        // p1 is going to survive as the ultimate final path.
        // paths: p1.end2 -> p1.end1  <join> p2.end1 -> p2.end2
        p1.end1 = p2.end2;
        p2.invalidate();
        pathsbyid.remove(p2.id);

        if (!p2.end2.isOutside) pathsongrid[p2.end2.x][p2.end2.y] = p1;
    }


}
