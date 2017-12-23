import java.awt.*;
import java.util.List;
import java.util.Vector;

/**
 * Created by chien on 5/6/2017.
 */
public class Board implements Solvable<Board>
{
    private GridFileReader gfr;
    private CellType[][] cells;
    private Regions regions;
    int numunknown;


    public Board(String fname)
    {
        this.gfr = new GridFileReader(fname);
        cells = new CellType[getWidth()][getHeight()];
        regions = new Regions();
        numunknown = getHeight() * getWidth();

        for (int x = 0 ; x < getWidth() ; ++x)
        {
            for (int y = 0 ; y < getHeight() ; ++y)
            {
                if (gfr.hasBlock("CELLS"))
                {
                    String s = gfr.getBlock("CELLS")[x][y];
                    if (s.equals("B")) { cells[x][y] = CellType.BRICK; --numunknown; }
                    else if (s.equals("F")) { cells[x][y] = CellType.FLOWERS; --numunknown; }
                    else if (s.equals("U")) cells[x][y] = CellType.UNKNOWN;
                    else throw new RuntimeException(("Unknown cell type " + s));
                }
                else
                {
                    cells[x][y] = CellType.UNKNOWN;
                }
                regions.addCellToRegion(getRegionId(x,y),x,y);
            }
        }
    }

    public Board(Board right)
    {
        gfr = right.gfr;
        cells = new CellType[getWidth()][getHeight()];
        regions = right.regions;
        numunknown = right.numunknown;

        for (int x = 0 ; x < getWidth() ; ++x)
        {
            for (int y = 0 ; y < getHeight() ; ++y)
            {
                cells[x][y] = right.cells[x][y];
            }
        }
    }


    int getWidth() { return gfr.getWidth(); }
    int getHeight() { return gfr.getHeight(); }

    char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    char getRegionId(int x,int y) { return gfr.getBlock("REGIONS")[x][y].charAt(0);}
    Regions getRegions() { return regions; }

    CellType getCell(int x,int y)
    {
        return cells[x][y];
    }

    void setCell(int x,int y,CellType type)
    {

        cells[x][y] = type;
        // efficiency...the only time we call setCell is to turn an unknown cell into either brick or flowers
        --numunknown;
    }

    @Override
    public boolean isSolution()
    {
        return numunknown == 0;
    }

    @Override
    public List<Board> Successors()
    {
        Vector<Board> result = new Vector<>();
        for (int x = 0 ; x < getWidth() ; ++x)
        {
            for (int y = 0 ; y < getHeight() ; ++y)
            {
                if (getCell(x,y) != CellType.UNKNOWN) continue;
                // we assume that there is at least one.
                Board brickBoard = new Board(this);
                brickBoard.setCell(x,y,CellType.BRICK);
                result.add(brickBoard);

                Board flowerBoard = new Board(this);
                flowerBoard.setCell(x,y,CellType.FLOWERS);
                result.add(flowerBoard);

                return result;
            }
        }
        throw new RuntimeException("Should never reach here!");
    }

    private static Point[] deltas = { new Point(1,0),new Point(0,1),new Point(-1,0),new Point(0,-1)};
    private static Point[] delta8 = {
            new Point(-1,-1),new Point(-1,0),new Point(-1,1),
            new Point(0,-1),                       new Point(0,1),
            new Point(1,-1),new Point(1,0), new Point(1,1)
    };

    public List<Point> getAdjacents(int x,int y,boolean diagonals)
    {
        Vector<Point> result = new Vector<>();
        Point[] mydeltas = diagonals ? delta8 : deltas;

        for (Point d : mydeltas)
        {
            Point np = new Point(d.x+x,d.y+y);
            if (np.x < 0 || np.y < 0 || np.x == getWidth() || np.y == getHeight()) continue;
            result.add(np);
        }
        return result;
    }




}
