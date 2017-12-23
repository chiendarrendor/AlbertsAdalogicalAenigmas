import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.*;
import java.util.Vector;

/**
 * Created by chien on 11/6/2017.
 */
public class QuadBlackLogicStep implements LogicStep<Board>
{
    int x;
    int y;


    public QuadBlackLogicStep(int x, int y) { this.x = x; this.y = y; }

    public LogicStatus apply(Board thing)
    {
        int whitecount = 0;
        int blackcount = 0;
        Vector<Point> unknowns = new Vector<>();


        for (int dx = 0 ; dx < 2 ; ++dx)
        {
            for (int dy = 0 ; dy < 2 ; ++dy)
            {
                Point p = new Point(x+dx,y+dy);
                switch(thing.getCell(p.x,p.y))
                {
                    case UNKNOWN: unknowns.add(p); break;
                    case BLACK: ++blackcount;  break;
                    case WHITE: ++whitecount; break;
                }
            }
        }

        if (blackcount == 4) return LogicStatus.CONTRADICTION;
        if (blackcount == 3 && unknowns.size() == 1)
        {
            Point wp = unknowns.firstElement();
            thing.setCell(wp.x,wp.y,CellState.WHITE);
            return LogicStatus.LOGICED;
        }
        return LogicStatus.STYMIED;
    }
}
