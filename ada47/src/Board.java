import grid.file.GridFileReader;
import grid.puzzlebits.Direction;

/**
 * Created by chien on 9/2/2017.
 */
public class Board
{
    private GridFileReader gfr;
    private EdgeType[][] hedges;
    private EdgeType[][] vedges;
    int unknowns;
    PathSet ps;
    
    public Board(String s)
    {
        gfr = new GridFileReader(s);
        hedges = new EdgeType[getWidth()][getHeight()-1];
        vedges = new EdgeType[getWidth()-1][getHeight()];
        unknowns = getWidth()*(getHeight()-1) + (getWidth()-1) * getHeight();

        forEachEdge( (e,iv,x,y) -> EdgeType.UNKNOWN);

        ps = new PathSet(this);

        ClueScanner.scan(this);

    }
    
    public Board(Board right)
    {
        gfr = right.gfr;
        hedges = new EdgeType[getWidth()][getHeight()-1];
        vedges = new EdgeType[getWidth()-1][getHeight()];
        unknowns = right.unknowns;

        forEachEdge( (e,iv,x,y) -> iv ? right.vedges[x][y] : right.hedges[x][y]);

        ps = new PathSet(this,right.ps);
    }
    

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight()
    {
        return gfr.getHeight();
    }
    public char getLetter(int cx, int cy)
    {
        return gfr.getBlock("LETTERS")[cx][cy].charAt(0);
    }
    public boolean hasLetter(int cx, int cy)
    {
        return getLetter(cx,cy) != '.';
    }

    public char getClue(int cx,int cy) { return gfr.getBlock("CLUES")[cx][cy].charAt(0);}


    private class EdgeCoord
    {
        EdgeType[][] bucket = null;
        int ex = -1;
        int ey = -1;

        public EdgeCoord(int cx,int cy,Direction dir)
        {
            switch(dir)
            {
                case NORTH:
                    bucket = hedges;
                    ex = cx;
                    ey = cy - 1;
                    break;
                case SOUTH:
                    bucket = hedges;
                    ex = cx;
                    ey = cy;
                    break;
                case WEST:
                    bucket = vedges;
                    ex = cx - 1;
                    ey = cy;
                    break;
                case EAST:
                    bucket = vedges;
                    ex = cx;
                    ey = cy;
                    break;
                default: throw new RuntimeException("EdgeCoord called with non-orthogonal Direction!");
            }
        }

        // returns true iff this points to an actual internal edge
        boolean isValid()
        {
            if (ex < 0 || ey < 0) return false;
            if (ex >= bucket.length) return false;
            if (ey >= bucket[0].length) return false;
            return true;
        }

        EdgeType get() { return bucket[ex][ey];}
        void set(EdgeType e) { bucket[ex][ey] = e;}

    }

    public boolean hasWall(int cx,int cy,Direction dir)
    {
        return getEdge(cx,cy,dir) == EdgeType.NOTPATH;
    }

    public boolean hasPath(int cx, int cy, Direction dir)
    {
        return getEdge(cx,cy,dir) == EdgeType.PATH;
    }

    public EdgeType getEdge(int cx,int cy,Direction dir)
    {
        EdgeCoord ec = new EdgeCoord(cx,cy,dir);
        if (!ec.isValid()) return EdgeType.NOTPATH;
        return ec.get();
    }

    public void setEdge(int cx,int cy,Direction dir,EdgeType et)
    {
        EdgeCoord ec = new EdgeCoord(cx,cy,dir);
        if (!ec.isValid()) throw new RuntimeException("Can't SetEdge on an invalid edge!");
        if (ec.get() != EdgeType.UNKNOWN) throw new RuntimeException("Can't SetEdge on a non-unknown edge!");
        ec.set(et);

        if (et == EdgeType.PATH) ps.AddPath(cx,cy,dir);

        --unknowns;
    }




    // Edge Lambda stuff
    public interface EdgeLambda
    {
        EdgeType operation(EdgeType oldval, boolean isV,int x,int y);
    }



    public void forEachEdge(EdgeLambda el)
    {
        forEachHEdge(el);
        forEachVEdge(el);
    }

    public void forEachHEdge(EdgeLambda el)
    {
        for (int x = 0 ; x < getWidth() ; ++x)
        {
            for (int y = 0 ; y < getHeight() - 1; ++y)
            {
                hedges[x][y] = el.operation(hedges[x][y],false,x,y);
            }
        }
    }

    public void forEachVEdge(EdgeLambda el)
    {
        for (int x = 0 ; x < getWidth() - 1 ; ++x)
        {
            for (int y = 0 ; y < getHeight() ; ++y)
            {
                vedges[x][y] = el.operation(vedges[x][y],true,x,y);
            }
        }
    }

    // cell lambda stuff
    public interface CellLambda
    {
        boolean operation(int x,int y);
    }

    public void forEachCell(CellLambda cl)
    {
        for (int x = 0 ; x < getWidth() ; ++x)
        {
            for (int y = 0 ; y < getHeight() ; ++y)
            {
                if (!cl.operation(x,y)) return;
            }
        }
    }
}
