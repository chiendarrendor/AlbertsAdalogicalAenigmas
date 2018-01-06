import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.awt.*;
import java.util.Vector;

public class StripLogicStep implements LogicStep<Board> {
    Vector<Point> strip = new Vector<>();
    int target;

    public StripLogicStep(int x, int y, Direction d, int slen, int targetcount)
    {
        for (int i = 0 ; i < slen ; ++i) strip.add(new Point(x+i*d.DX(), y+i*d.DY()));
        this.target = targetcount;
    }

    @Override
    public LogicStatus apply(Board thing) {
        Vector<Point> unknowns = new Vector<>();
        int oncount = 0;
        int offcount = 0;

        for (Point p : strip)
        {
            switch(thing.getCell(p.x,p.y))
            {
                case ONPATH: ++oncount; break;
                case NOTPATH: ++offcount; break;
                case UNKNOWN: unknowns.add(p); break;
            }
        }
        if (oncount > target) return LogicStatus.CONTRADICTION;
        if (unknowns.size() == 0) return oncount < target ? LogicStatus.CONTRADICTION : LogicStatus.STYMIED;

        if (oncount == target)
        {
            for (Point p : unknowns) thing.setCell(p.x,p.y,CellType.NOTPATH);
            return LogicStatus.LOGICED;
        }

        if (oncount + unknowns.size() == target)
        {
            for (Point p : unknowns) thing.setCell(p.x,p.y,CellType.ONPATH);
            return LogicStatus.LOGICED;
        }

        return LogicStatus.STYMIED;
    }
}
