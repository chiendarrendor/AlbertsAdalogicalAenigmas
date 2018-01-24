import grid.puzzlebits.Direction;

public class CellQuery
{
    Board b;
    int x;
    int y;
    Direction incomingdir;
    Direction startdir;
    Direction oppdir;
    Direction ortho1;
    Direction ortho2;

    // so we followed path to this cell in incomingdir direction....
    // which means we know it has a path to it, and therefore one has to leave...
    // what do we know about this cell now?
    public CellQuery(Board b,int x,int y, Direction incomingdir)
    {
        this.b = b;
        this.x = x;
        this.y = y;
        this.incomingdir = incomingdir;
        this.oppdir = incomingdir;
        this.startdir = incomingdir.getOpp();
        for (Direction d: Direction.orthogonals())
        {
            if (d != startdir && d != oppdir)
            {
                ortho1 = d;
                ortho2 = d.getOpp();
                break;
            }
        }
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public boolean goesStraight() { return b.getEdge(x,y,oppdir) == EdgeType.PATH; }
    public boolean canGoStraight() { return b.getEdge(x,y,oppdir) == EdgeType.UNKNOWN; }
    public boolean bends() { return b.getEdge(x,y,ortho1) == EdgeType.PATH || b.getEdge(x,y,ortho2) == EdgeType.PATH; }
    public boolean canBend() { return b.getEdge(x,y,ortho1) == EdgeType.UNKNOWN || b.getEdge(x,y,ortho2) == EdgeType.UNKNOWN; }
    public boolean canBendOneWay() { return canBend() && (b.getEdge(x,y,ortho1) == EdgeType.NOTPATH || b.getEdge(x,y,ortho2) == EdgeType.NOTPATH);}
    public Direction uniqueBendDirection()
    {
        if (!canBendOneWay()) throw new RuntimeException("should not call uniqueBendDirection unless there is a unique bend!");
        return b.getEdge(x,y,ortho1) == EdgeType.UNKNOWN ? ortho1 : ortho2;
    }



}
