import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.*;
import java.util.List;

public class CrownLogicStep implements LogicStep<Board>
{
    List<Point> points;
    int targetweight;
    String info;
    public CrownLogicStep(int targetweight,List<Point> points,String info) { this.points = points; this.targetweight = targetweight; this.info = info; }

    public LogicStatus apply(Board thing)
    {
        // this gives us the set of all possible places where the crown could be
        SegmentBreaker segs = new SegmentBreaker(thing,points,CellType.WHITE,false,(s)->{ return s.weight(thing) >= targetweight; });
        if (targetweight > 0 && segs.segments.size() == 0) return LogicStatus.CONTRADICTION;

        for (Segment seg : segs.segments)
        {
            // this will return, for this region containing only BLACK and UNKNOWN cells, the sets of BLACK cells that are too big.
            SegmentBreaker segons = new SegmentBreaker(thing,seg.getPoints(),CellType.UNKNOWN,false,(s)-> { return s.weight(thing) > targetweight; });
            if (segons.segments.size() > 0) return LogicStatus.CONTRADICTION;

        }

        return LogicStatus.STYMIED;
    }

    public String toString() { return "CrownLogicStep " + info; }

}
