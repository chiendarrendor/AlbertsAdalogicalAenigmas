import java.awt.*;
import java.util.Vector;

/**
 * Created by chien on 5/6/2017.
 */
public class RegionLogicStep implements LogicStep<Board>
{
    @Override
    public LogicStatus apply(Board thing)
    {
        LogicStatus result = LogicStatus.STYMIED;

        for (Region r : thing.getRegions().values())
        {
            LogicStatus step = applyOneRegion(r,thing);
            if (step == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
            if (step == LogicStatus.LOGICED) result = LogicStatus.LOGICED;

        }

        return result;
    }

    private LogicStatus applyOneRegion(Region r, Board thing)
    {
        int numbricks = 0;
        int numunknown = 0;
        Vector<Point> unknowns = new Vector<>();

        for (Point p : r.getCells())
        {
            switch(thing.getCell(p.x,p.y))
            {
                case BRICK: ++numbricks; break;
                case FLOWERS: break;
                case UNKNOWN:
                    ++numunknown;
                    unknowns.add(p);
                    break;
            }
        }

        if (numbricks > 2) return LogicStatus.CONTRADICTION;
        if (numbricks + numunknown < 2) return LogicStatus.CONTRADICTION;
        if (numunknown == 0) return LogicStatus.STYMIED;

        // if we get here, there are unknown cells, the region can still be made legal...is there something we can do?
        // if we already have the right # of bricks, everything else is flowers.
        if (numbricks == 2)
        {
            for (Point p: unknowns)
            {
                thing.setCell(p.x, p.y, CellType.FLOWERS);
            }
            return LogicStatus.LOGICED;
        }

        // if we already have the right # of flowers, everything else is bricks
        if (numbricks + numunknown == 2)
        {
            for (Point p: unknowns)
            {
                thing.setCell(p.x, p.y, CellType.BRICK);
            }
            return LogicStatus.LOGICED;
        }
        return LogicStatus.STYMIED;
    }
}
