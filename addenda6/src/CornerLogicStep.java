import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.*;
import java.util.Collection;
import java.util.Vector;

/**
 * Created by chien on 8/17/2017.
 */
public class CornerLogicStep implements LogicStep<LogicBoard>
{
    int value;
    Collection<Point> adjacents;


    public CornerLogicStep(int value, Collection<Point> adjacents)
    {
        this.value = value;
        this.adjacents = adjacents;
    }

    private void ShowStatus(LogicBoard thing,String message)
    {
        System.out.println("Failed: " + message);
        for (Point p : adjacents)
        {
            System.out.println("x: " + p.x + " y: " + p.y + ": " + thing.getCell(p.x,p.y));
        }

    }



    public LogicStatus apply(LogicBoard thing)
    {
        int landcount = 0;
        Vector<Point>  unknowns = new Vector<Point>();
        for (Point p : adjacents)
        {
            if (thing.getCell(p.x,p.y) == CellState.LAND) ++landcount;
            if (thing.getCell(p.x,p.y) == CellState.UNKWNOWN) unknowns.add(p);
        }

        if (landcount > value)
        {
//            ShowStatus(thing,"too many lands already");
            return LogicStatus.CONTRADICTION;
        }
        if (landcount + unknowns.size() < value)
        {
//            ShowStatus(thing,"Not enough lands available");
            return LogicStatus.CONTRADICTION;
        }

        if (unknowns.size() == 0) return LogicStatus.STYMIED;

        if (landcount == value)
        {
            for (Point p : unknowns)
            {
                thing.setCellRiver(p.x,p.y);
            }
            return LogicStatus.LOGICED;
        }

        if (landcount + unknowns.size() == value)
        {
            for (Point p : unknowns)
            {
                thing.setCellLand(p.x,p.y);
            }
            return LogicStatus.LOGICED;
        }

        return LogicStatus.STYMIED;
    }
}
