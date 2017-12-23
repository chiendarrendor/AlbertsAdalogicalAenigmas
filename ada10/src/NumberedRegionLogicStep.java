import java.awt.*;
import java.util.Vector;

/**
 * Created by chien on 4/22/2017.
 */
public class NumberedRegionLogicStep implements LogicStep<Board>
{
    int number;
    Vector<Point> cells;
    String designator;

    public NumberedRegionLogicStep(String designator, int number,Vector<Point> cells)
    {
        this.designator = designator;
        this.number = number;
        this.cells = cells;
    }

    @Override
    public LogicStatus apply(Board thing)
    {
        int treecount = 0;
        int emptycount = 0;
        Vector<Point> unknowns = new Vector<Point>();

        for (Point p : cells)
        {
            switch(thing.getCell(p.x,p.y))
            {
                case TREE: ++treecount; break;
                case UNKNOWN: unknowns.add(p);
                case EMPTY: ++emptycount; break;
            }
        }

        if (treecount > number) return LogicStatus.CONTRADICTION;
        if (treecount + unknowns.size() < number) return LogicStatus.CONTRADICTION;
        if (unknowns.size() == 0) return LogicStatus.STYMIED;

        if (treecount == number)
        {
            for (Point p : unknowns) { thing.setCell(p.x,p.y,CellType.EMPTY); }
            return LogicStatus.LOGICED;

        }

        if (treecount + unknowns.size() == number)
        {
            for (Point p : unknowns) { thing.setCell(p.x,p.y,CellType.TREE); }
            return LogicStatus.LOGICED;
        }

        return LogicStatus.STYMIED;

    }

    public String toString() { return "NumberedRegionLogicStep " + designator; }

}
