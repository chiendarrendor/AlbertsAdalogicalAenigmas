import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.*;
import java.util.List;

public class CakeLogicStep implements LogicStep<Board>
{
    int blockcount;
    List<Point> points;
    boolean[] adjtoblack;

    public CakeLogicStep(int blockcount, List<Point> points)
    {
        this.blockcount = blockcount;
        this.points = points;
        adjtoblack = new boolean[points.size()];
    }


    @Override
    public LogicStatus apply(Board thing)
    {
        // this gets a list of all white-broken areas with at least one black cell inside.
        // this is the _minimum_ number of possible sections.
        SegmentBreaker sb1 = new SegmentBreaker(thing,points,CellType.WHITE,false,(s)->
        {
            for (Point p : s.getPoints()) if (thing.getCell(p.x,p.y) == CellType.BLACK) return true;
            return false;
        });
        if (sb1.segments.size() > blockcount) return LogicStatus.CONTRADICTION;

        // we now want to calculate the _largest_ possible number of broken areas.
        // a pure black area has a count of 1
        // breaking all black-unknown-black areas on their unknown edges maximizes possible size of areas
        // any pure unknown area of even size has a count of size/2
        // any pure unknown area of odd size has a count of size+1/2
        // if the maximum # of unknown areas is not large enough, it's a contradiction
        for(int i = 0 ; i < adjtoblack.length ; ++i)  adjtoblack[i] = false;
        for(int i = 0 ; i < adjtoblack.length-1 ; ++i)
        {
            Point p = points.get(i);
            CellType ct = thing.getCell(p.x,p.y);
            if (ct == CellType.BLACK) adjtoblack[i+1] = true;
        }
        for (int i = 1 ; i < adjtoblack.length ; ++i)
        {
            Point p = points.get(i);
            CellType ct = thing.getCell(p.x,p.y);
            if (ct == CellType.BLACK) adjtoblack[i-1] = true;
        }

        SegmentBreaker sb2 = new SegmentBreaker((p,i)->{
            if(thing.getCell(p.x,p.y) == CellType.UNKNOWN && adjtoblack[i]) return CellType.WHITE;
            return thing.getCell(p.x,p.y);
        },points,CellType.WHITE);
        // invariant time:
        // all groups that would be <black><unknown><black> must now be broken because at least one unknown turned to white
        // which means that if a group as any black in it, it's all black.
        // which means that the only groups that are anything else are all unknown.
        int gcount = 0;
        for(Segment s : sb2.segments)
        {
            Point fp = s.getPoints().get(0);
            CellType ct = thing.getCell(fp.x,fp.y);
            if (ct == CellType.BLACK) { ++gcount; continue; }

            gcount += (s.getPoints().size()+1)/2;
        }
        if (gcount < blockcount) return LogicStatus.CONTRADICTION;



        return LogicStatus.STYMIED;
    }
}
