import javafx.scene.control.Cell;

import java.awt.*;
import java.util.List;
import java.util.Vector;

/**
 * Created by chien on 5/6/2017.
 */
public class BrickLogicStep implements LogicStep<Board>
{
    @Override
    public LogicStatus apply(Board thing)
    {
        LogicStatus result = LogicStatus.STYMIED;

        for (int x = 0 ; x < thing.getWidth() ; ++x)
        {
            for (int y = 0 ; y < thing.getHeight() ; ++y)
            {
                if (thing.getCell(x,y) != CellType.BRICK) continue;
                LogicStatus step = applyToOneBrick(thing,x,y);
                if (step == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
                if (step == LogicStatus.LOGICED) result = LogicStatus.LOGICED;
            }
        }
        return result;
    }

    private LogicStatus applyToOneBrick(Board thing, int x, int y)
    {
        int numbricks = 0;
        int numunknowns = 0;
        List<Point> adjacents = thing.getAdjacents(x,y,false);
        List<Point> unknowns = new Vector<Point>();

        for (Point p : adjacents)
        {
            switch(thing.getCell(p.x,p.y))
            {
                case BRICK: ++numbricks; break;
                case FLOWERS: break;
                case UNKNOWN:
                    ++numunknowns;
                    unknowns.add(p);
                    break;
            }
        }

        if (numbricks > 1) return LogicStatus.CONTRADICTION;
        if (numbricks + numunknowns < 1) return LogicStatus.CONTRADICTION;
        if (numunknowns == 0) return LogicStatus.STYMIED;

        if (numbricks == 1)
        {
            for (Point p : unknowns)
            {
                thing.setCell(p.x,p.y, CellType.FLOWERS);
            }
            return LogicStatus.LOGICED;
        }

        if (numbricks + numunknowns == 1)
        {
            for (Point p : unknowns)
            {
                thing.setCell(p.x,p.y,CellType.BRICK);
            }
            return LogicStatus.LOGICED;
        }

        return LogicStatus.STYMIED;


    }
}
