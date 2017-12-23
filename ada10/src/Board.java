import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

/**
 * Created by chien on 4/20/2017.
 */
public class Board implements Solvable<Board>
{
    private GridFileReader gfr;

    private CellType[][] trees;


    public Board(GridFileReader gfr)
    {
        this.gfr = gfr;
        trees = new CellType[getWidth()][getHeight()];
        for (int x = 0 ; x < getWidth() ; ++ x)
        {
            for (int y = 0 ; y < getHeight() ; ++y)
            {
                trees[x][y] = CellType.UNKNOWN;
            }
        }
    }

    public Board(Board right)
    {
        gfr = right.gfr;
        trees = new CellType[getWidth()][getHeight()];
        for (int x = 0 ; x < getWidth() ; ++ x)
        {
            for (int y = 0 ; y < getHeight() ; ++y)
            {
                trees[x][y] = right.trees[x][y];
            }
        }
    }

    public CellType getCell(int x,int y) { return trees[x][y]; }
    public void setCell(int x,int y,CellType c) { trees[x][y] = c; }



    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }

    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.'; }
    public char getLetter(int x, int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }

    public boolean hasNumber(int x,int y) { return getNumber(x,y) != '.'; }
    public char getNumber(int x, int y) { return gfr.getBlock("NUMBERS")[x][y].charAt(0); }


    public char getRegionId(int x,int y) { return gfr.getBlock("AREAS")[x][y].charAt(0);}

    Point[] deltas = { new Point(0,1),new Point(1,0),new Point(0,-1), new Point(-1,0)};

    Vector<Point> adjacents(int x, int y)
    {
        Vector<Point> result = new Vector<Point>();
        for (Point delta : deltas)
        {
            Point p = new Point(x+delta.x,y+delta.y);
            if (p.x < 0 || p.y < 0 || p.x >= getWidth() || p.y >= getHeight()) continue;
            result.add(p);
        }
        return result;
    }




    @Override
    public boolean isSolution()
    {
        for (int x = 0 ; x < getWidth() ; ++x)
        {
            for (int y = 0 ; y < getHeight() ; ++y)
            {
                if (getCell(x,y) == CellType.UNKNOWN) return false;
            }
        }
        return true;
    }

    @Override
    public List<Board> Successors()
    {
        List<Board> result = new Vector<Board>();

        for (int x = 0 ; x < getWidth() ; ++x)
        {
            for (int y = 0 ; y < getHeight() ; ++y)
            {
                if (getCell(x,y) != CellType.UNKNOWN) continue;

                Board b1 = new Board(this);
                b1.setCell(x,y,CellType.TREE);
                Board b2 = new Board(this);
                b2.setCell(x,y,CellType.EMPTY);
                result.add(b1);
                result.add(b2);
                return result;


            }
        }
        // should never get here.
        return null;
    }

    Set<Point> path = new HashSet<>();

    public void setPath(List<Point> path) { for (Point p : path) { this.path.add(p);} }
    public boolean onPath(int x,int y) { return path.contains(new Point(x,y));}



}
