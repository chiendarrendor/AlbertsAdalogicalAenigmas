import grid.graph.GridGraph;
import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.awt.*;
import java.util.Set;

public class PaintLogicStep implements LogicStep<Board>
{
    int x;
    int y;
    int weight;

    private class MyGridReference implements GridGraph.GridReference
    {
        Board b;
        CellType type;
        boolean isOptimistic;
        public MyGridReference(Board b,CellType type,boolean isOptimistic) { this.b = b; this.type = type; this.isOptimistic = isOptimistic; }
        public int getWidth() { return b.getWidth(); }
        public int getHeight() { return b.getHeight(); }
        public boolean edgeExitsEast(int x, int y) { return true; }
        public boolean edgeExitsSouth(int x,int y) { return true; }

        public boolean isIncludedCell(int x, int y)
        {
            CellType ct = b.getCell(x,y);
            if(ct == type) return true;
            if (ct == CellType.UNKNOWN && isOptimistic) return true;
            return false;
        }


    }

    public PaintLogicStep(int x, int y, int weight) { this.x = x; this.y = y; this.weight = weight; }
    private int weight(Board b,Set<Point> points)
    {
        return points.stream().mapToInt((p)->{return b.getWeight(p.x,p.y);}).sum();
    }

    @Override
    public LogicStatus apply(Board thing)
    {
        CellType ct = thing.getCell(x,y);
        if (ct == CellType.UNKNOWN) return LogicStatus.STYMIED;

        GridGraph ggopt = new GridGraph(new MyGridReference(thing,ct,true));
        GridGraph ggpes = new GridGraph(new MyGridReference(thing,ct,false));
        Set<Point> optset = ggopt.connectedSetOf(new Point(x,y));
        Set<Point> pesset = ggpes.connectedSetOf(new Point(x,y));

        if (weight(thing,pesset) > weight) return LogicStatus.CONTRADICTION;
        if (weight(thing,optset) < weight) return LogicStatus.CONTRADICTION;

        LogicStatus result = LogicStatus.STYMIED;

        // if the pessimistic set (only ons) is the right size, make all adjacents to the pessimistic set
        // that are unknowns into not ons
        if (weight(thing,pesset) == weight)
        {
            for(Point p : pesset)
            {
                for (Direction d : Direction.orthogonals())
                {
                    int adjx = p.x + d.DX();
                    int adjy = p.y + d.DY();
                    if (!thing.inBounds(adjx,adjy)) continue;
                    if (thing.getCell(adjx,adjy) != CellType.UNKNOWN) continue;
                    thing.setCell(adjx,adjy,ct == CellType.BLACK ? CellType.WHITE : CellType.BLACK);
                    result = LogicStatus.LOGICED;
                }
            }
        }

        // if the optmistic set is the right size, then all unknowns in the
        // optimistic set have to be ons.
        if (weight(thing,optset) == weight)
        {
            for (Point p : optset)
            {
                if (thing.getCell(p.x,p.y) != CellType.UNKNOWN) continue;
                result = LogicStatus.LOGICED;
                thing.setCell(p.x,p.y,ct);
            }
        }




        return result;
    }
}
