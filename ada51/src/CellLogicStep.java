import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.util.Vector;

// this class will not have to handle any starting cells, since there is no cell logic to be done on them.
public class CellLogicStep implements LogicStep<Board>
{
    int x;
    int y;

    public CellLogicStep(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    @Override
    public LogicStatus apply(Board thing) {
        switch (thing.getCell(x, y)) {
            case NOTPATH: return applyOubliette(thing);
            case ONPATH: return applyThroughPath(thing);
            case UNKNOWN: return applyUnknown(thing);
            default: throw new RuntimeException("what fresh cell is this?");
        }
    }

    private LogicStatus applyOubliette(Board thing)
    {
        LogicStatus result = LogicStatus.STYMIED;
        for (Direction d : Direction.orthogonals())
        {
            switch (thing.getEdge(x,y,d))
            {
                case PATH: return LogicStatus.CONTRADICTION;
                case NOTPATH: break;
                case UNKNOWN:
                    thing.setEdge(x,y,d,EdgeType.NOTPATH);
                    result = LogicStatus.LOGICED;
                    break;
            }
        }
        return result;
    }

    Vector<Direction> unknowns = new Vector<>();
    int pathcount;
    int wallcount;

    private void processEdges(Board thing)
    {
        pathcount = 0;
        wallcount = 0;
        unknowns.clear();

        for (Direction d : Direction.orthogonals())
        {
            switch (thing.getEdge(x,y,d))
            {
                case PATH: ++pathcount; break;
                case NOTPATH: ++wallcount; break;
                case UNKNOWN: unknowns.add(d); break;
            }
        }
    }

    private LogicStatus setUnknowns(Board thing,EdgeType et) { for(Direction d : unknowns) thing.setEdge(x,y,d,et); return LogicStatus.LOGICED; }



    private LogicStatus applyThroughPath(Board thing)
    {
        processEdges(thing);
        if (pathcount > 2) return LogicStatus.CONTRADICTION;
        if (wallcount > 2) return LogicStatus.CONTRADICTION;
        if (unknowns.size() == 0) return LogicStatus.STYMIED;

        if (pathcount == 2) return setUnknowns(thing,EdgeType.NOTPATH);
        if (pathcount + unknowns.size() == 2) return setUnknowns(thing,EdgeType.PATH);
        return LogicStatus.STYMIED;


    }

    private LogicStatus applyUnknown(Board thing)
    {
        processEdges(thing);
        // a) can we tell if this is an oubliette?
        if (wallcount > 2)
        {
            if (pathcount > 0) return LogicStatus.CONTRADICTION;
            thing.setCell(x,y,CellType.NOTPATH);
            // doesn't matter how many unknowns we have (1 or 0), we're still LOGICED
            setUnknowns(thing,EdgeType.NOTPATH);
            // at the very least, we know that the cell was unknown and now it's NOTPATH
            return LogicStatus.LOGICED;
        }
        if (pathcount > 2) return LogicStatus.CONTRADICTION;
        // if we get here, both pathcount and wallcount max out at 2

        // we know we have a path
        if (pathcount > 0)
        {
            thing.setCell(x,y,CellType.ONPATH);
            if (pathcount == 2) setUnknowns(thing,EdgeType.NOTPATH);
            if (pathcount + unknowns.size() == 2) setUnknowns(thing,EdgeType.PATH);
            return LogicStatus.LOGICED;
        }

        // if pathcount == 0 and wallcount <= 2, we have no idea whether this is path or oubliette....
        return LogicStatus.STYMIED;
    }

}
