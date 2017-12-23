import java.awt.*;
import java.util.Map;
import java.util.Vector;

/**
 * Created by chien on 5/20/2017.
 */

    // this class, when handed a set of points
    // (may assume that they will be ordered along some singular positive or negative grid axis)
    // will organize them into blocks of adjacent Points.
    // an odd sized block of size n may hold no more than n/2 + 1 black cells
    // an even sized block of size n may hold no more than n/2 black cells
    // this creates a more stringent limit on validating clues

public class EmptyAdjacencyBlocks
{
    private class Block
    {
        Vector<Point> points = new Vector<>();
        public void add(Point p) { points.add(p); }
        public int maxBlackCells()
        {
            return points.size() / 2  + (points.size() % 2 == 0 ? 0 : 1);
        }

        public int blackFill(Board b)
        {
            if (points.size() % 2 == 0) return 0;
            int result = 0;
            for (int i = 0 ; i < points.size() ; ++i)
            {
                ++result;
                if (i % 2 == 0)
                {
                    b.setCellBlack(points.elementAt(i).x,points.elementAt(i).y);
                }
                else
                {
                    b.setCellWhite(points.elementAt(i).x,points.elementAt(i).y);
                }
            }
            return result;
        }

        public int whiteFill(Board b)
        {
            int result = 0;
            for (int i = 0 ; i < points.size() ; ++i)
            {
                ++result;
                b.setCellWhite(points.elementAt(i).x,points.elementAt(i).y);
            }
            return result;
        }
    }

    private Vector<Block> blocks = new Vector<>();

    public EmptyAdjacencyBlocks()
    {
        blocks.add(new Block());
    }

    private Block curBlock() { return blocks.lastElement(); }

    private Point prevPoint()
    {
        Block cb = curBlock();
        if (cb.points.size() == 0) return null;
        return cb.points.lastElement();
    }

    private boolean isAdjacent(Point p1,Point p2)
    {
        return Math.abs(p1.x - p2.x) == 1 || Math.abs(p1.y - p2.y) == 1;
    }

    public void addCell(Point p)
    {
        Point pp = prevPoint();
        if (pp == null || isAdjacent(pp,p))
        {
            curBlock().add(p);
        }
        else
        {
            blocks.add(new Block());
            curBlock().add(p);
        }
    }

    public int maxBlackCount()
    {
        int result = 0;
        for(Block b : blocks) { result += b.maxBlackCells(); }
        return result;
    }

    public int blackFill(Board b)
    {
        int result = 0;
        for (Block blk : blocks)
        {
            result += blk.blackFill(b);
        }
        return result;
    }

    public int whiteFill(Board b)
    {
        int result = 0;
        for (Block blk : blocks)
        {
            result += blk.whiteFill(b);
        }
        return result;
    }



}
