import grid.file.GridFileReader;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.MultiFlattenSolvable;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;
import grid.puzzlebits.EdgeContainer;
import grid.puzzlebits.Path.GridPathContainer;
import grid.puzzlebits.Path.Path;

import java.awt.*;
import java.util.List;
import java.util.Vector;

public class Board extends CellContainer<CellType> implements MultiFlattenSolvable<Board>
{
    GridFileReader gfr;
    EdgeContainer<EdgeType> ec;
    GridPathContainer gpc;

    int unknowncells;
    int unknownedges;


    public Board(String fname) { this(new GridFileReader(fname));}

    private Board(GridFileReader gfr)
    {
        super(gfr.getWidth(),gfr.getHeight(),(x,y)-> { return CellType.UNKNOWN; }, (x,y,r) -> { return r; });

        ec = new EdgeContainer<EdgeType>(gfr.getWidth(),gfr.getHeight(),
                (x,y,isV)->{
                    if (x == 0 && isV) return EdgeType.NOTPATH;
                    if (y == 0 && !isV) return EdgeType.NOTPATH;
                    if (x == gfr.getWidth()) return EdgeType.NOTPATH;
                    if (y == gfr.getHeight()) return EdgeType.NOTPATH;
                    return EdgeType.UNKNOWN;
                },
                (x,y,isV,old)->{ return old;}
        );
        this.gfr = gfr;

        gpc = new GridPathContainer(getWidth(),getHeight(),(x,y,cell)-> {
           if (cell.getInternalPaths().size() > 0) throw new BadMergeException("merging with middle of other path!");
           if (cell.getTerminalPaths().size() > 2) throw new BadMergeException("merging more than two!");
           if (cell.getTerminalPaths().size() == 1) return;

           Path p1 = cell.getTerminalPaths().get(0);
           Path p2 = cell.getTerminalPaths().get(1);

           if (p1 == p2) cell.closeLoop(p1);
           else cell.merge(p1,p2);
        });

        unknowncells = getWidth() * getHeight();
        unknownedges = (getWidth()-1) * getHeight() + getWidth() * (getHeight() - 1);




        ClueScanner.scan(this);

    }

    public Board(Board right)
    {
        super(right);
        ec = new EdgeContainer<EdgeType>(right.ec);
        gpc = new GridPathContainer(right.gpc);
        this.gfr = right.gfr;
        unknownedges = right.unknownedges;
        unknowncells = right.unknowncells;
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }



    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public char getClue(int x,int y) { return gfr.getBlock("CLUES")[x][y].charAt(0); }
    public int getVClue(int x) { return Integer.parseInt(gfr.getBlock("VCLUES")[x][0]); }
    public int getHClue(int y) { return Integer.parseInt(gfr.getBlock( "HCLUES")[0][y]); }
    public EdgeType getEdge(int x, int y, Direction d) { return ec.getEdge(x,y,d); }
    public boolean isTerminal(int x,int y) { return "^Vv<>".indexOf(getClue(x,y)) != -1; }
    // setting these off because they will have implications re pathing and CellContainers and EdgeContainers are agnostic of such.
    public void setEdge(int x,int y,Direction d,EdgeType et)
    {
        --unknownedges;
        ec.setEdge(x,y,d,et);
        if (et != EdgeType.PATH) return;

        setCell(x,y,CellType.ONPATH);
        setCell(x+d.DX(),y+d.DY(),CellType.ONPATH);
        gpc.link(new Point(x,y),new Point(x+d.DX(),y+d.DY()));
    }

    public void setCell(int x,int y,CellType ct)
    {
        if (getCell(x,y) != CellType.UNKNOWN) return;
        --unknowncells;
        super.setCell(x,y,ct);
    }

    // FlattenSolvable stuff
    @Override
    public boolean isComplete() { return unknowncells == 0 && unknownedges == 0; }

    private class MyMove
    {
        private boolean isCell;
        private CellType ct;
        private EdgeType et;
        private Direction d;
        private int x;
        private int y;

        public MyMove(int x,int y,CellType ct) { isCell = true; this.x = x; this.y = y; this.ct = ct; }
        public MyMove(int x,int y,Direction d,EdgeType et) { isCell = false; this.x = x; this.y = y; this.d = d; this.et = et; }

        public boolean applyMove(Board thing)
        {
            return (isCell ? applyCellMove(thing) : applyEdgeMove(thing));
        }

        private boolean applyEdgeMove(Board thing)
        {
            CellType curct = thing.getCell(x,y);
            CellType ocurct = thing.getCell(x+d.DX(), y+d.DY());
            EdgeType curet = thing.getEdge(x,y,d);

            if (et == EdgeType.PATH)
            {
                if (curct == CellType.NOTPATH) return false;
                if (ocurct == CellType.NOTPATH) return false;
            }

            if (curet != EdgeType.UNKNOWN) return curet == et;
            thing.setEdge(x,y,d,et);
            return true;
        }

        private boolean applyCellMove(Board thing)
        {
            CellType curct = thing.getCell(x,y);
            if (curct != CellType.UNKNOWN) return curct == ct;
            thing.setCell(x,y,ct);
            return true;
        }
    }





    @Override
    public boolean applyMove(Object o)
    {
        MyMove mm = (MyMove)o;
        return mm.applyMove(this);
    }

    FlattenSolvableTuple<Board> makeFST(MyMove mm1,MyMove mm2)
    {
        Board b1 = new Board(this);
        Board b2 = new Board(this);
        if (!mm1.applyMove(b1)) return null;
        if (!mm2.applyMove(b2)) return null;
        return new FlattenSolvableTuple<Board>(b1,mm1,b2,mm2);
    }



    @Override
    public List<FlattenSolvableTuple<Board>> getTuplesForCell(int x, int y)
    {
        Vector<FlattenSolvableTuple<Board>> result = new Vector<>();

        for (Direction d : Direction.orthogonals())
        {
            if (getEdge(x,y,d) != EdgeType.UNKNOWN) continue;
            FlattenSolvableTuple<Board> fst = makeFST(new MyMove(x,y,d,EdgeType.PATH),new MyMove(x,y,d,EdgeType.NOTPATH));
            if (fst != null) result.add(fst);

        }

        if (getCell(x,y) == CellType.UNKNOWN)
        {
            FlattenSolvableTuple<Board> fst = makeFST(new MyMove(x,y,CellType.ONPATH),new MyMove(x,y,CellType.NOTPATH));
            if (fst != null) result.add(fst);
        }

        return result;
    }


}
