import grid.file.GridFileReader;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.MultiFlattenSolvable;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;
import grid.puzzlebits.EdgeContainer;
import grid.puzzlebits.Path.GridPathContainer;
import grid.puzzlebits.Path.Path;

import java.util.ArrayList;
import java.util.List;

import java.awt.*;

public class Board  implements MultiFlattenSolvable<Board>
{
    GridFileReader gfr;
    CellContainer<CellType> cellPathInfo;
    CellContainer<CellColor> cellColors;
    EdgeContainer<EdgeType> edges;
    GridPathContainer gpc;

    private int COLORS = 0;
    private int EDGES = 1;
    private int CELLS = 2;
    int[] unknowns = new int[3];

    public Board(String fname)
    {
        gfr = new GridFileReader(fname);
        unknowns[COLORS] = 0;
        unknowns[EDGES] = 0;
        unknowns[CELLS] = 0;

        cellPathInfo = new CellContainer<CellType>(getWidth(),getHeight(),
            (x,y)-> {
                if (getClue(x,y) == '.')
                {
                    ++unknowns[CELLS];
                    return CellType.UNKNOWN;
                }
                else
                {
                    return CellType.ONPATH;
                }
            },
            (x,y,r)->{ return r; }
        );

        cellColors = new CellContainer<CellColor>(getWidth(),getHeight(),
            (x,y)-> {
                switch(getClue(x,y))
                {
                    case 'W': return CellColor.WHITE;
                    case 'B': return CellColor.BLACK;
                    case '.': return CellColor.UNCOLORED;
                    case 'o':
                        ++unknowns[COLORS];
                        return CellColor.UNKNOWN;
                    default:
                        throw new RuntimeException("Unknown Color type!");
                }
            },
            (x,y,r)->{ return r; }
        );

        edges = new EdgeContainer<EdgeType>(gfr.getWidth(),gfr.getHeight(),
                (x,y,isV)->{
                    if (x == 0 && isV) return EdgeType.NOTPATH;
                    if (y == 0 && !isV) return EdgeType.NOTPATH;
                    if (x == gfr.getWidth()) return EdgeType.NOTPATH;
                    if (y == gfr.getHeight()) return EdgeType.NOTPATH;
                    ++unknowns[EDGES];
                    return EdgeType.UNKNOWN;
                },
                (x,y,isV,old)->{ return old;}
        );

        gpc = new GridPathContainer(getWidth(),getHeight(),(x,y,cell)-> {
            if (cell.getInternalPaths().size() > 0) throw new BadMergeException("merging with middle of other path!");
            if (cell.getTerminalPaths().size() > 2) throw new BadMergeException("merging more than two!");
            if (cell.getTerminalPaths().size() == 1) return;

            Path p1 = cell.getTerminalPaths().get(0);
            Path p2 = cell.getTerminalPaths().get(1);

            if (p1 == p2) cell.closeLoop(p1);
            else cell.merge(p1,p2);
        });
    }

    public Board(Board right)
    {
        gfr = right.gfr;
        cellPathInfo = new CellContainer<CellType>(right.cellPathInfo);
        cellColors = new CellContainer<CellColor>(right.cellColors);
        edges = new EdgeContainer<EdgeType>(right.edges);
        gpc = new GridPathContainer(right.gpc);
        unknowns = right.unknowns.clone();
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public char getClue(int x,int y) { return gfr.getBlock("CIRCLES")[x][y].charAt(0); }

    public CellType getCell(int x,int y) { return cellPathInfo.getCell(x,y); }
    public CellColor getCellColor(int x,int y) { return cellColors.getCell(x,y); }
    public EdgeType getEdge(int x,int y,Direction d) { return edges.getEdge(x,y,d); }

    public void setCellColor(int x,int y, CellColor c)
    {
        --unknowns[COLORS];
        cellColors.setCell(x,y,c);
    }

    public void setCell(int x,int y,CellType c)
    {
        if (getCell(x,y) != CellType.UNKNOWN) return;
        --unknowns[CELLS];
        cellPathInfo.setCell(x,y,c);
    }

    public void setEdge(int x,int y,Direction d,EdgeType et)
    {
        if (getEdge(x,y,d) != EdgeType.UNKNOWN) throw new RuntimeException("Duplicate Edge Set!");
        --unknowns[EDGES];
        edges.setEdge(x,y,d,et);
        if (et == EdgeType.NOTPATH) return;

        setCell(x,y,CellType.ONPATH);
        setCell(x+d.DX(),y+d.DY(),CellType.ONPATH);
        gpc.link(new Point(x,y),new Point(x+d.DX(),y+d.DY()));
    }

    // multiflattensolvable stuff
    public boolean isComplete() { return unknowns[CELLS] == 0 && unknowns[EDGES] == 0 && unknowns[COLORS] == 0; }
    public boolean applyMove(Object o)
    {
        MyMove mm = (MyMove)o;
        return mm.applyMove(this);
    }

    private FlattenSolvableTuple<Board> makeFST(MyMove mm1,MyMove mm2)
    {
        Board b1 = new Board(this);
        if (!mm1.applyMove(b1)) return null;
        Board b2 = new Board(this);
        if (!mm2.applyMove(b2)) return null;
        return new FlattenSolvableTuple<Board>(b1,mm1,b2,mm2);
    }

    public List<FlattenSolvableTuple<Board>> getTuplesForCell(int x,int y)
    {
        ArrayList<FlattenSolvableTuple<Board>> result = new ArrayList<>();

        for (Direction d : Direction.orthogonals())
        {
            if (getEdge(x,y,d) != EdgeType.UNKNOWN) continue;
            FlattenSolvableTuple<Board> fst = makeFST(new MyMove(x,y,d,EdgeType.PATH),new MyMove(x,y,d,EdgeType.NOTPATH));
            if (fst != null) result.add(fst);
        }

        if (getCellColor(x,y) == CellColor.UNKNOWN)
        {
            FlattenSolvableTuple<Board> fst = makeFST(new MyMove(x,y,CellColor.BLACK),new MyMove(x,y,CellColor.WHITE));
            if (fst != null) result.add(fst);
        }

        if (getCell(x,y) == CellType.UNKNOWN)
        {
            FlattenSolvableTuple<Board> fst = makeFST(new MyMove(x,y,CellType.NOTPATH),new MyMove(x,y,CellType.ONPATH));
            if (fst != null) result.add(fst);
        }

        return result;
    }
}
