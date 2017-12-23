import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.awt.*;
import java.util.Vector;

/**
 * Created by chien on 8/13/2017.
 */
public class ArrowLogicStep implements LogicStep<LogicBoard>
{
    int px = -1;
    int py = -1;
    Vector<Point> others = new Vector<>();


    public ArrowLogicStep(LogicBoard b, int x, int y)
    {
        Direction d = b.getArrow(x,y);

        for (Point p : b.getAdjacentCells(x,y))
        {

            if (p.x == x + d.DX() && p.y == y + d.DY())
            {
                px = p.x;
                py = p.y;
            }
            else
            {
                others.add(p);
            }
        }

        if (px == -1 || py == -1) throw new RuntimeException("ArrowLogicStep can't find arrow destination: " + x + " " + y);
    }

    @Override
    public LogicStatus apply(LogicBoard thing)
    {
        LogicStatus result = LogicStatus.STYMIED;
        Board.IntegerSet largeis = thing.getPossibles(px,py);

        // the pointed cell must be larger than the smallest of any other cell
        int smallest = 100;
        for (Point p : others)
        {
            int ts = thing.getPossibles(p.x,p.y).getSmallest();
            if (ts < smallest) smallest = ts;
        }
        if (smallest == 100) return LogicStatus.CONTRADICTION;
        if (largeis.keepLargerThan(smallest)) result = LogicStatus.LOGICED;

        // all other cells must be smaller than the largest of pointed
        int lval = largeis.getLargest();
        if (lval == -1) return LogicStatus.CONTRADICTION;

        for (Point p : others)
        {
            if (thing.getPossibles(p.x,p.y).keepSmallerThan(lval)) result = LogicStatus.LOGICED;
        }

        return null;
    }
}
