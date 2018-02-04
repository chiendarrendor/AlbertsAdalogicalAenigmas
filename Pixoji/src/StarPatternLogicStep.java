import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.awt.*;
import java.util.*;
import java.util.List;

public class StarPatternLogicStep implements LogicStep<Board> {
    int count;
    private enum StarState { YES,MAYBE,NO };
    private class StarCellInfo
    {
        private Point p;
        private CellType right;
        public Point getP() { return p; }
        public CellType right() { return right; }
        public CellType wrong() { return right == CellType.BLACK ? CellType.WHITE : CellType.BLACK; }
        public StarCellInfo(Point p,CellType right) { this.p = p; this.right = right; }
        public boolean isRight(Board thing) { return thing.getCell(p.x,p.y) == right(); }
        public boolean isWrong(Board thing) { return thing.getCell(p.x,p.y) == wrong(); }
    }

    List<StarCellInfo> getStar(Board b,Point p,CellType starEdge)
    {
        List<StarCellInfo> result = new ArrayList<>();
        result.add(new StarCellInfo(p,CellType.BLACK));
        for (Direction d : Direction.orthogonals())
        {
            Point np = new Point(p.x+d.DX(),p.y+d.DY());
            if (!b.inBounds(np.x,np.y)) continue;
            result.add(new StarCellInfo(np,starEdge));
        }
        return result;
    }

    Map<Point,List<StarCellInfo>> stars = new HashMap<>();

    private StarState isStar(Point p,Board thing)
    {
        StarState result = StarState.YES;
        Collection<StarCellInfo> cells = stars.get(p);
        for (StarCellInfo sti : cells)
        {
            if (sti.isWrong(thing)) return StarState.NO;
            if (!sti.isRight(thing)) result = StarState.MAYBE;
        }
        return result;
    }

    // makestar is easy...all cells must be right to be a star.
    private LogicStatus makeStar(Board thing, Point p)
    {
        LogicStatus result = LogicStatus.STYMIED;
        Collection<StarCellInfo> cells = stars.get(p);
        for (StarCellInfo sti : cells)
        {
            if (sti.isWrong(thing)) return LogicStatus.CONTRADICTION;
            if (sti.isRight(thing)) continue;
            thing.setCell(sti.getP().x,sti.getP().y,sti.right());
            result = LogicStatus.LOGICED;
        }
        return result;
    }

    // breakstar is a little harder...any cell can be wrong.
    private LogicStatus breakStar(Board thing, Point p)
    {
        Collection<StarCellInfo> cells = stars.get(p);
        int rightcount = 0;
        int wrongcount = 0;
        List<StarCellInfo> unknowns = new ArrayList<>();
        for (StarCellInfo sti : cells)
        {
            if (sti.isRight(thing)) ++rightcount;
            else if (sti.isWrong(thing)) ++wrongcount;
            else unknowns.add(sti);
        }
        // only one way to be wrong here...if we actually have a star..
        if (wrongcount == 0 && unknowns.size() == 0) return LogicStatus.CONTRADICTION;
        if (wrongcount > 0) return LogicStatus.STYMIED;
        if (unknowns.size() > 1) return LogicStatus.STYMIED;
        // we only get here if wrongcount == 0 (i.e. we have to make something wrong)
        // and unknowns.size() == 1 (i.e. we have only one thing we can make wrong.
        StarCellInfo sti = unknowns.get(0);
        thing.setCell(sti.getP().x,sti.getP().y,sti.wrong());
        return LogicStatus.LOGICED;
    }




    public StarPatternLogicStep(Board b, int size, CellType starEdge, Collection<Point> points)
    {
        this.count = size;
        for (Point p : points) stars.put(p,getStar(b,p,starEdge));
    }

    @Override
    public LogicStatus apply(Board thing)
    {
        LogicStatus result = LogicStatus.STYMIED;
        int yesweight = 0;
        int noweight = 0;
        List<Point> unknowns = new ArrayList<Point>();
        int unkweight = 0;

        for (Point p : stars.keySet())
        {
            switch(isStar(p,thing))
            {
                case YES: yesweight += thing.getWeight(p.x,p.y); break;
                case MAYBE: unknowns.add(p); unkweight += thing.getWeight(p.x,p.y); break;
                case NO: noweight += thing.getWeight(p.x,p.y); break;
            }
        }

        if (yesweight > count) return LogicStatus.CONTRADICTION;
        if (yesweight + unkweight < count) return LogicStatus.CONTRADICTION;
        if (unkweight == 0) return LogicStatus.STYMIED;

        if (yesweight == count)
        {
            for(Point p : unknowns)
            {
                switch(breakStar(thing,p))
                {
                    case LOGICED: result = LogicStatus.LOGICED; break;
                    case STYMIED: break;
                    case CONTRADICTION: return LogicStatus.CONTRADICTION;
                }
            }
        }
        if (yesweight + unkweight == count)
        {
            for(Point p : unknowns)
            {
                switch(makeStar(thing,p))
                {
                    case LOGICED: result = LogicStatus.LOGICED; break;
                    case STYMIED: break;
                    case CONTRADICTION: return LogicStatus.CONTRADICTION;
                }
            }
        }




        return result;
    }
}
