import grid.file.GridFileReader;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.MultiFlattenSolvable;
import grid.puzzlebits.Direction;

import java.util.*;

/**
 * Created by chien on 10/18/2017.
 */
public class Board extends MultiFlattenSolvable<Board> implements RegionSelector
{
    private GridFileReader gfr;
    private RegionManager regions;
    private PathManager pathmanager;
    private EdgeSet edgeset;

    public Board(String fname)
    {
        gfr = new GridFileReader(fname);
        pathmanager = new PathManager(getWidth(),getHeight());
        edgeset = new EdgeSet(getWidth(),getHeight());
        regions = new RegionManager(this);
    }

    public Board(Board right)
    {
        this.gfr = right.gfr;
        pathmanager = new PathManager(right.getPathManager());
        edgeset = new EdgeSet(right.getEdgeSet());
        regions = right.getRegionManager();
    }

    private RegionManager getRegionManager() { return regions; }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }

    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.'; }
    public char getRegionId(int x,int y) { return gfr.getBlock("REGIONS")[x][y].charAt(0); }

    public int getRegionExpectedCellCount(char regionid)
    {
        if (!gfr.hasVar(""+regionid)) return -1;
        return GridFileReader.toInt(gfr.getVar(""+regionid));
    }

    public Set<Character> getRegionIds()
    {
        return regions.getRegionIds();
    }
    public Region getRegion(char regionid) { return regions.get(regionid); }

    public boolean isComplete()
    {
        return isFilled() && getPathManager().isLoopPerfect();
    }

    public PathManager getPathManager() { return pathmanager; }
    public PathState getPathState(int x,int y) { return getPathManager().getPathState(x,y); }
    public void setPathState(int x, int y, PathState ps) { getPathManager().setPathState(x,y,ps); }

    public EdgeSet getEdgeSet() { return edgeset; }
    public boolean isFilled() { return getEdgeSet().isFilled(); }
    public EdgeType getEdge(int x, int y, Direction d) { return getEdgeSet().getEdge(x,y,d);}
    public void setEdgeWall(int x,int y,Direction d) { getEdgeSet().setEdge(x,y,d,EdgeType.WALL);}
    // returns false if pathing prevents destination from taking an edge
    // must cause path state to be set for both cells.
    public boolean setEdgePath(int x, int y, Direction d)
    {
        getEdgeSet().setEdge(x,y,d,EdgeType.PATH);
        return getPathManager().joinTo(x,y,d);
    }

    private class MyEdgeMove
    {
        int x;
        int y;
        Direction d;
        boolean isWall;
        public MyEdgeMove(int x, int y, Direction d, boolean isWall)
        {
            this.x = x; this.y = y; this.d = d; this.isWall = isWall;
        }
    }

    private class MyCellMove
    {
        int x;
        int y;
        boolean isOnPath;
        public MyCellMove(int x,int y, boolean isOnPath) { this.x = x; this.y = y; this.isOnPath = isOnPath; }
    }



    public void applyMove(Object o)
    {
        if (o instanceof MyEdgeMove)
        {
            MyEdgeMove mm = (MyEdgeMove)o;
            if (mm.isWall) setEdgeWall(mm.x, mm.y, mm.d);
            else setEdgePath(mm.x, mm.y, mm.d);
            return;
        }

        if (o instanceof MyCellMove)
        {
            MyCellMove mcm = (MyCellMove)o;
            setPathState(mcm.x,mcm.y,mcm.isOnPath ? PathState.ONPATH : PathState.OFFPATH);
            return;
        }
        throw new RuntimeException("Unknown move in applymove!");
    }

    @Override
    public List<FlattenSolvableTuple<Board>> getTuplesForCell(int x, int y)
    {
        Vector<FlattenSolvableTuple<Board>> result = new Vector<>();

        if (x < getWidth() - 1)
        {
            FlattenSolvableTuple<Board> fst = makeOne(x, y, Direction.EAST);
            if (fst != null) result.add(fst);
        }

        if (y < getHeight() - 1)
        {
            FlattenSolvableTuple<Board> fst = makeOne(x, y, Direction.SOUTH);
            if (fst != null) result.add(fst);
        }

        if (getPathState(x,y) == PathState.UNKNOWN)
        {
            Board b1 = new Board(this);
            b1.setPathState(x,y,PathState.ONPATH);
            MyCellMove mcm1 = new MyCellMove(x,y,true);
            Board b2 = new Board(this);
            b2.setPathState(x,y,PathState.OFFPATH);
            MyCellMove mcm2 = new MyCellMove(x,y,false);
            result.add(new FlattenSolvableTuple<Board>(b1,mcm1,b2,mcm2));
        }




        return result;
    }

    private FlattenSolvableTuple<Board> makeOne(int x, int y, Direction d)
    {

        if (getEdge(x,y,d) != EdgeType.UNKNOWN) return null;
        Board b1 = new Board(this);
        b1.setEdgePath(x,y,d);
        MyEdgeMove mm1 = new MyEdgeMove(x,y,d,false);

        Board b2 = new Board(this);
        b2.setEdgeWall(x,y,d);
        MyEdgeMove mm2 = new MyEdgeMove(x,y,d,true);


        FlattenSolvableTuple<Board> result = new FlattenSolvableTuple<Board>(b1,mm1,b2,mm2);

        return result;
    }

}
