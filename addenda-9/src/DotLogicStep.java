import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;
import grid.puzzlebits.Path.GridPathCell;
import grid.puzzlebits.Path.Path;
import grid.puzzlebits.Turns;
import sun.rmi.runtime.Log;

import java.awt.*;
import java.util.List;

public class DotLogicStep implements LogicStep<Board>
{
    int x;
    int y;

    public DotLogicStep(int x, int y) { this.x = x; this.y = y; }

    private LogicStatus applyBlackCell(Board thing)
    {
        LogicStatus result = LogicStatus.STYMIED;
        // 1) any edge that leaves this cell must be extended if it isn't already.
        //    we'll check if that creates the proper bend in a moment, but
        //    we want the next section to operate only on proper pair-jumps
        for (Direction d : Direction.orthogonals())
        {
            if (thing.getEdge(x,y,d) != EdgeType.PATH) continue;
            switch(thing.getEdge(x+d.DX(),y+d.DY(),d)) {
                case PATH:
                    break;
                case NOTPATH:
//                    System.out.println("DotLogicStep Black can't extend existing path out");
                    return LogicStatus.CONTRADICTION;
                case UNKNOWN:
                    thing.setEdge(x + d.DX(), y + d.DY(), d, EdgeType.PATH);
                    result = LogicStatus.LOGICED;
                    break;
            }
        }
        // 2) any open edge from here that can't make the double jump should be closed
        for (Direction d : Direction.orthogonals())
        {
            if (thing.getEdge(x,y,d) != EdgeType.UNKNOWN) continue;
            switch(thing.getEdge(x+d.DX(),y+d.DY(),d)) {
                case PATH:
                case UNKNOWN:
                    break;
                case NOTPATH:
                    thing.setEdge(x , y , d, EdgeType.NOTPATH);
                    result = LogicStatus.LOGICED;
                    break;
            }
        }

        // if we get here, each direction out from here should be one of:
        // NOTPATH from here
        // doublejump-capable from here
        // doublejumped already from here
        //
        // so we should be able to take any NOTPATH edge and verify that
        // we either already have, or can, extend out opposite.
        // (note, this quite naturally causes un-bent paths through this cell to fail!)

        for (Direction d : Direction.orthogonals())
        {
            Direction od = d.getOpp();
            if (thing.getEdge(x,y,d) != EdgeType.NOTPATH) continue;
            if (thing.getEdge(x,y,od) == EdgeType.NOTPATH)
            {
//                System.out.println("DotLogic BLACK NOTPATH direction " + d + " has opposite NOTPATH");
                return LogicStatus.CONTRADICTION;
            }
            if (thing.getEdge(x,y,od) == EdgeType.PATH) continue;
            thing.setEdge(x,y,od,EdgeType.PATH);

            if (thing.getEdge(x+od.DX(),y+od.DY(),od) == EdgeType.UNKNOWN) {
                thing.setEdge(x + od.DX(), y + od.DY(), od, EdgeType.PATH);
            }
            result = LogicStatus.LOGICED;
        }




        return result;
    }


    private LogicStatus applyWhiteCell(Board thing)
    {
        LogicStatus result = LogicStatus.STYMIED;
        int pathcount = 0;
        Direction pathdir = null;

        for (Direction d : Direction.orthogonals())
        {
            Direction od = d.getOpp();
            EdgeType mye = thing.getEdge(x,y,d);
            EdgeType oe = thing.getEdge(x,y,od);

            switch(mye)
            {
                case UNKNOWN: break;
                case PATH:
                    ++pathcount;
                    pathdir = d;
                    switch(oe)
                    {
                        case PATH: break;
                        case NOTPATH: return LogicStatus.CONTRADICTION;
                        case UNKNOWN: thing.setEdge(x,y,od,EdgeType.PATH); result = LogicStatus.LOGICED; break;
                    }
                    break;
                case NOTPATH:
                    switch(oe)
                    {
                        case NOTPATH: break;
                        case PATH: return LogicStatus.CONTRADICTION;
                        case UNKNOWN: thing.setEdge(x,y,od,EdgeType.NOTPATH); result = LogicStatus.LOGICED; break;
                    }
                    break;
            }
        }

        // No more than one adjacent cell on the path to a white may be a straight-through
        // if we don't have paths on both ends, let's stop until we do.
        // (if pathcount is non-zero and we got here, we have two paths, by the way...
        if (pathcount == 0) return result;
        CellQuery cq1 = new CellQuery(thing,x+pathdir.DX(),y+pathdir.DY(),pathdir);
        CellQuery cq2 = new CellQuery(thing,x+pathdir.getOpp().DX(),y+pathdir.getOpp().DY(),pathdir.getOpp());

        if (cq1.goesStraight()) {
            if (cq2.bends()) return result;
            if (!cq2.canBend()) return LogicStatus.CONTRADICTION;
            if (!cq2.canBendOneWay()) return result;
            thing.setEdge(cq2.getX(), cq2.getY(), cq2.uniqueBendDirection(), EdgeType.PATH);
            return LogicStatus.LOGICED;
        }

        if (cq2.goesStraight()) {
            if (cq1.bends()) return result;
            if (!cq1.canBend()) return LogicStatus.CONTRADICTION;
            if (!cq1.canBendOneWay()) return result;
            thing.setEdge(cq1.getX(),cq1.getY(),cq1.uniqueBendDirection(),EdgeType.PATH);
            return LogicStatus.LOGICED;
        }



        return result;
    }
    private LogicStatus applyUnknownCell(Board thing) { return LogicStatus.STYMIED; }



    @Override
    public LogicStatus apply(Board thing)
    {
        try
        {
            thing.gpc.clean();
        }
        catch(BadMergeException bme)
        {
//            System.out.println("DotLogic Path Clean Failure! " + bme );
            return LogicStatus.CONTRADICTION;
        }

        switch(thing.getCellColor(x,y))
        {
            case BLACK: return applyBlackCell(thing);
            case WHITE: return applyWhiteCell(thing);
            case UNKNOWN: return applyUnknownCell(thing);
            default: throw new RuntimeException("why?");
        }
    }

    public String toString() { return "DotLogicStep (" + x + "," + y + ")";}
}
