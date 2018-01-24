import grid.puzzlebits.Direction;

public class MyMove
{
    private enum MoveType { CELL,EDGE,COLOR };
    private MoveType type;
    private int x;
    private int y;
    private Direction d;
    private CellType ct;
    private CellColor cc;
    private EdgeType et;

    public MyMove(int x,int y,CellType ct) { type = MoveType.CELL; this.x = x; this.y = y; this.ct = ct; }
    public MyMove(int x,int y,CellColor cc) { type = MoveType.COLOR; this.x = x; this.y = y; this.cc = cc; }
    public MyMove(int x,int y,Direction d,EdgeType et) { type = MoveType.EDGE; this.x = x; this.y = y; this.d = d; this.et = et; }

    public boolean applyMove(Board thing)
    {
        switch(type)
        {
            case CELL: return applyCellMove(thing);
            case EDGE: return applyEdgeMove(thing);
            case COLOR: return applyColorMove(thing);
            default: throw new RuntimeException("bwah?");
        }
    }

    private boolean applyCellMove(Board thing)
    {
        if (thing.getCell(x,y) != CellType.UNKNOWN) return thing.getCell(x,y) == ct;
        thing.setCell(x,y,ct);
        return true;
    }

    private boolean applyColorMove(Board thing)
    {
        if (thing.getCellColor(x,y) != CellColor.UNKNOWN) return thing.getCellColor(x,y) == cc;
        thing.setCellColor(x,y,cc);
        return true;
    }

    private boolean applyEdgeMove(Board thing)
    {
        CellType curcell = thing.getCell(x,y);
        CellType ocurcell = thing.getCell(x+d.DX(),y+d.DY());
        EdgeType curet = thing.getEdge(x,y,d);

        if (et == EdgeType.PATH) {
            if (curcell == CellType.NOTPATH) return false;
            if (ocurcell == CellType.NOTPATH) return false;
        }
        if (curet != EdgeType.UNKNOWN) return curet == et;
        thing.setEdge(x,y,d,et);
        return true;
    }

}
