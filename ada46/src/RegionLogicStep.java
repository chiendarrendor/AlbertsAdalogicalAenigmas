import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.*;
import java.util.*;

/**
 * Created by chien on 8/13/2017.
 */
public class RegionLogicStep implements LogicStep<LogicBoard>
{
    Collection<Point> cells;

    public RegionLogicStep(LogicBoard b, Character rid)
    {
        cells = b.getRegionPoints(rid);
    }

    @Override
    public LogicStatus apply(LogicBoard thing)
    {
        LogicStatus result = LogicStatus.STYMIED;

        // if we know a cell in a region has a known value, every other cell in that
        // region can't have it.
        for (Point p : cells)
        {
            Board.IntegerSet is = thing.getPossibles(p.x,p.y);
            if (is.size() == 0) return LogicStatus.CONTRADICTION;
            if (is.size() > 1) continue;
            int val = is.getSingular();
            for (Point inp : cells)
            {
                if (p == inp) continue;
                Board.IntegerSet inps = thing.getPossibles(inp.x,inp.y);
                if (!inps.contains(val)) continue;
                inps.remove(val);
                result = LogicStatus.LOGICED;
            }
        }

        // every value must be covered by a cell.
        Map<Integer,Vector<Point>> cellsByCover = new HashMap<Integer,Vector<Point>>();
        for(int i = 1 ; i <= cells.size() ; ++i) cellsByCover.put(i,new Vector<Point>());

        for(Point p : cells)
        {
            Board.IntegerSet is = thing.getPossibles(p.x,p.y);
            for (Integer i : is)
            {
                cellsByCover.get(i).add(p);
            }
        }

        for (int i = 1 ; i <= cells.size() ; ++i)
        {
            Vector<Point> pointsForId = cellsByCover.get(i);
            if (pointsForId.size() == 0) return LogicStatus.CONTRADICTION;
            if (pointsForId.size() > 1) continue;
            Point uniqueCell = pointsForId.firstElement();

            for (Point p : cells)
            {
                if (p == uniqueCell)
                {
                    if (thing.getPossibles(p.x,p.y).size() == 1) continue;
                    thing.getPossibles(p.x,p.y).removeAllBut(i);
                    result = LogicStatus.LOGICED;
                }
                else
                {
                    if (!thing.getPossibles(p.x,p.y).contains(i)) continue;
                    thing.getPossibles(p.x,p.y).remove(i);
                    result = LogicStatus.LOGICED;
                }
            }
        }
        return result;
    }
}
