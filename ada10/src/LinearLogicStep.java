import java.awt.*;

/**
 * Created by chien on 4/23/2017.
 */
public class LinearLogicStep implements LogicStep<Board>
{
    Point[] points;


    public LinearLogicStep(Point start, Point delta, int count)
    {
        points = new Point[count];
        for (int i = 0 ; i < count ; ++i)
        {
            points[i] = new Point(start.x + i * delta.x,start.y + i * delta.y);
        }
    }

    @Override
    public LogicStatus apply(Board thing)
    {
        LogicStatus result = LogicStatus.STYMIED;
        // 1) find groups of adjacent EMPTY spaces; each one can be processed individually and independently.
        boolean inGroup = false;
        int start = -1;

        for (int i = 0 ; i < points.length ; ++i)
        {
            Point curp = points[i];
            CellType ct = thing.getCell(curp.x,curp.y);
            if (inGroup == false && ct == CellType.EMPTY)
            {
                start = i;
                inGroup = true;
            }
            else if (inGroup == true && ct != CellType.EMPTY)
            {
                inGroup = false;
 //               System.out.println("found adjacent group between (" + points[start].x + "," + points[start].y + ") and (" + points[i-1].x + "," + points[i-1].y + ")");
                LogicStatus subr = applySingle(start,i-1,thing);
                if (subr == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
                if (subr == LogicStatus.LOGICED) result = LogicStatus.LOGICED;
            }
        }

        if (inGroup == true)
        {
//           System.out.println("found adjacent group between (" + points[start].x + "," + points[start].y + ") and (" + points[points.length-1].x + "," + points[points.length-1].y + ")");
            LogicStatus subr = applySingle(start,points.length-1,thing);
            if (subr == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
            if (subr == LogicStatus.LOGICED) result = LogicStatus.LOGICED;
        }

        return result;
    }

    private LogicStatus processBefore(int idx,Board thing)
    {
        if (idx == 0) return LogicStatus.STYMIED;
        Point pMe = points[idx];
        Point pBefore = points[idx-1];
        if (thing.getCell(pBefore.x,pBefore.y) == CellType.TREE) return LogicStatus.STYMIED;

        if (thing.getRegionId(pMe.x,pMe.y) != thing.getRegionId(pBefore.x,pBefore.y))
        {
            thing.setCell(pBefore.x,pBefore.y,CellType.TREE);
            return LogicStatus.LOGICED;
        }
        return LogicStatus.STYMIED;
    }

    private LogicStatus processAfter(int idx,Board thing)
    {
        if (idx == points.length - 1) return LogicStatus.STYMIED;
        Point pMe = points[idx];
        Point pAfter = points[idx+1];
        if (thing.getCell(pAfter.x,pAfter.y) == CellType.TREE) return LogicStatus.STYMIED;

        if (thing.getRegionId(pMe.x,pMe.y) != thing.getRegionId(pAfter.x,pAfter.y))
        {
            thing.setCell(pAfter.x,pAfter.y,CellType.TREE);
            return LogicStatus.LOGICED;
        }
        return LogicStatus.STYMIED;
    }


    private LogicStatus applySingle(int start, int end, Board thing)
    {
        // count number of region transitions
        int rtrans = 0;
        for (int i = start ; i < end ; ++i)
        {
            Point p1 = points[i];
            Point p2 = points[i+1];
            if (thing.getRegionId(p1.x,p1.y) != thing.getRegionId(p2.x,p2.y)) ++rtrans;
        }

        if (rtrans > 1) return LogicStatus.CONTRADICTION;
        if (rtrans == 0) return LogicStatus.STYMIED;

        // we are looking at a region of empty spaces that cannot cross another region boundary.
        // for both ends, we need to see if they are
        // a) locked (i.e. edge of board or tree is keeping an extension from occurring in that direction
        // b) dangerous (an extension in that direction crosses a boundary)
        // c) safe (neither locked nor safe)
        LogicStatus result = LogicStatus.STYMIED;
        if (processBefore(start,thing) == LogicStatus.LOGICED) result = LogicStatus.LOGICED;
        if (processAfter(end,thing) == LogicStatus.LOGICED) result = LogicStatus.LOGICED;

        return result;
    }
}
