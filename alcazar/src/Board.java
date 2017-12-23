import grid.file.GridFileReader;
import grid.puzzlebits.Direction;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Created by chien on 7/23/2017.
 */
public class Board
{
    public class OutsidePair
    {
        CellInfo ci;
        Direction dir;

        public OutsidePair(CellInfo ci,Direction dir) { this.ci = ci; this.dir = dir; }
    }


    // constant data about a cell
    public class CellInfo
    {
        boolean isOutside = false;
        boolean isOptional = false;
        int x;
        int y; // x,y coordinate only valid for non-outside cells.
        // not set for outside, otherwise contains a record for each orthogonal direction
        // containing the adjacent CellInfo (outside if we look through an exit)
        // or null if a link is not valid in that direction.
        Map<Direction,CellInfo> adjacents = new HashMap<Direction,CellInfo>();

        public CellInfo(int x,int y) { this.x = x ; this.y = y; }
        public CellInfo() { isOutside = true; }

        public String toString()
        {
            if (isOutside) return "OUTSIDE";
            return "(" + x + "," + y + ")";
        }

        public CellInfo getAdjacent(Direction d) { return adjacents.get(d);}


        Vector<OutsidePair> opairs = new Vector<>();

        public void addOutsideAdjacent(CellInfo ci,Direction d)
        {
            opairs.add(new OutsidePair(ci,d));
        }

    }

    CellInfo[][] ci;
    CellInfo outside;
    GridFileReader gfr;

    CellState[][] cs;
    PathManager pm;

    public Board(String fname)
    {
        gfr = new GridFileReader(fname);
        ci = new CellInfo[getWidth()][getHeight()];
        outside = new CellInfo();

        for(int x = 0 ; x < getWidth() ; ++x )
        {
            for (int y = 0; y < getHeight(); ++y)
            {
                ci[x][y] = new CellInfo(x, y);
                ci[x][y].isOptional = gfr.getBlock("OPTIONALS")[x][y].equals("O");
            }
        }

        for(int x = 0 ; x < getWidth() ; ++x )
        {
            for (int y = 0; y < getHeight(); ++y)
            {
                for (Direction dir : Direction.orthogonals())
                {
                    int nx = x + dir.DX();
                    int ny = y + dir.DY();
                    CellInfo adj = null;

                    if (inBounds(nx,ny)) adj = ci[nx][ny];
                    else if (dir.getShort().equals(gfr.getBlock("EXITS")[x][y]))
                    {
                        adj = outside;
                        outside.addOutsideAdjacent(ci[x][y],dir);
                    }
                    ci[x][y].adjacents.put(dir,adj);
                }
            }
        }

        for(int x = 0 ; x < getWidth() ; ++x )
        {
            for (int y = 0; y < getHeight(); ++y)
            {
                CellInfo cci = ci[x][y];

                String edges = gfr.getBlock("EDGES")[x][y];
                if (edges.equals(".")) continue;

                for (Direction dir : Direction.orthogonals())
                {
                    CellInfo aci = cci.getAdjacent(dir);
                    if (aci == null || aci.isOutside) continue;
                    if (edges.indexOf(dir.getShort()) != -1)
                    {
                        cci.adjacents.put(dir,null);
                        aci.adjacents.put(dir.getOpp(),null);
                    }
                }
            }
        }

        cs = new CellState[getWidth()][getHeight()];
        for(int x = 0 ; x < getWidth() ; ++x )
        {
            for (int y = 0; y < getHeight(); ++y)
            {
                CellInfo cci = ci[x][y];
                cs[x][y] = new CellState();

                for (Direction dir : Direction.orthogonals())
                {
                    if( cci.getAdjacent(dir) == null ) cs[x][y].set(dir,EdgeState.WALL);
                }
            }
        }

        pm = new PathManager(this);

    }

    public Board(Board right)
    {
        gfr = right.gfr;
        this.ci = right.ci;
        this.outside = right.outside;

        cs = new CellState[getWidth()][getHeight()];
        for(int x = 0 ; x < getWidth() ; ++x )
        {
            for (int y = 0; y < getHeight(); ++y)
            {
                cs[x][y] = new CellState(right.cs[x][y]);
            }
        }
        pm = new PathManager(right.pm);
    }


    public PathManager getPathManager() { return pm; }
    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public boolean inBounds(int x,int y) { return gfr.inBounds(x,y); }
    public CellInfo getCI(int x,int y) { return ci[x][y]; }
    public boolean isOutside(CellInfo ci) { return ci == outside; }

    public EdgeState getEdge(int x,int y,Direction d) { return cs[x][y].get(d); }

    public void wall(int x,int y,Direction d)
    {
        set(x,y,d,EdgeState.WALL);
    }
    public void path(int x,int y,Direction d)
    {
        CellInfo cci = getCI(x,y);
        CellInfo nci = cci.getAdjacent(d);

        if (!set(x,y,d,EdgeState.PATH)) return;

        if (isOutside(nci)) getPathManager().Terminate(cci);
        else getPathManager().Merge(cci,nci,this);


    }


    private boolean set(int x,int y,Direction d,EdgeState es)
    {
//        System.out.println("X: " + x + " Y: " + y + " DIR: " + d + " State: " + es + " Cur: " + getEdge(x,y,d));

        if (getEdge(x,y,d) != EdgeState.UNKNOWN) return false;

        CellState curs = cs[x][y];
        CellInfo  curi = ci[x][y];
        CellInfo ai = curi.getAdjacent(d);

        curs.set(d,es);

        if (ai == null) throw new RuntimeException("should never try to set an impossible direction!");
        if (ai.isOutside) return true;

        CellState as = cs[ai.x][ai.y];
        as.set(d.getOpp(),es);
        return true;
    }
}
