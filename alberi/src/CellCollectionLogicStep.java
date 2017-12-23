import grid.logic.LogicStep;
import grid.logic.simple.LogicStatus;

import java.awt.*;
import java.util.List;
import java.util.Vector;

/**
 * Created by chien on 5/27/2017.
 */
public class CellCollectionLogicStep implements LogicStep<Board>
{
    List<Point> points;
    int count;

    public CellCollectionLogicStep(List<Point> points,int count) { this.points = points; this.count = count; }


    @Override
    public LogicStatus apply(Board thing)
    {
        int treecount = 0;
        Vector<Point> unknowns = new Vector<Point>();

        for (Point p : points)
        {
            switch(thing.getCell(p.x,p.y))
            {
                case TREE: ++treecount; break;
                case GRASS: break;
                case UNKNOWN: unknowns.add(p); break;
            }
        }

        if(treecount > count) return LogicStatus.CONTRADICTION;
        if(treecount + unknowns.size() < count) return LogicStatus.CONTRADICTION;
        if(unknowns.size() == 0) return LogicStatus.STYMIED;

        if (treecount == count)
        {
            for (Point p : unknowns) { thing.setCellGrass(p.x,p.y); }
            return LogicStatus.LOGICED;
        }

        if (treecount + unknowns.size() == count)
        {
            for (Point p : unknowns) { thing.setCellTree(p.x,p.y); }
            return LogicStatus.LOGICED;
        }

        return LogicStatus.STYMIED;
    }
}
