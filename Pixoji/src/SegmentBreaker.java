import java.awt.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

public class SegmentBreaker
{
    // given a set of Points, a Board, a breaking Cell Type, and a boolean
    // return a set of Segments each of which contain a longest possible distinct
    // adjacent set of points that do not contain the breaking cell type on the board.
    // if the boolean is true, also include Segments that contain the breaking cell types
    // also can be added a list of IncludeI objects that can reject a Segment if the op returns false

    public List<Segment> segments = new ArrayList<Segment>();
    public interface IncludeI { boolean op(Segment s); }
    public interface CellI { CellType op(Point p,int index); }
    private static IncludeI skipBreakers = (s)->{return !s.isBreaker(); };
    private static IncludeI noop = (s)->{ return true; };
    private static <T> T[] append(T[] base, T first)
    {
        T[] result = Arrays.copyOf(base,base.length+1);
        for (int i = 0 ; i < base.length ; ++i) result[i+1]=base[i];
        result[0] = first;
        return result;

    }

    private static class BoardI implements CellI
    {
        Board b;
        public BoardI(Board b) { this.b = b; }
        public CellType op(Point p,int index) { return b.getCell(p.x,p.y); }
    }


    private boolean isIncluded(Segment s,IncludeI[] filters)
    {
        for (IncludeI ii : filters ) if (!ii.op(s)) return false;
        return true;
    }


    public SegmentBreaker(Board b,List<Point> points,CellType breaker) { this(new BoardI(b),points,breaker); }
    public SegmentBreaker(Board b,List<Point> points,CellType breaker, boolean includeBreakers) { this(new BoardI(b),points,breaker,includeBreakers); }
    public SegmentBreaker(Board b,List<Point> points,CellType breaker,boolean includeBreakers,IncludeI ...infilters) { this(new BoardI(b),points,breaker,includeBreakers,infilters);}
    public SegmentBreaker(Board b, List<Point> points,CellType breaker,IncludeI ...infilters) { this(new BoardI(b),points,breaker,infilters); }



    public SegmentBreaker(CellI cib,List<Point> points,CellType breaker) { this(cib,points,breaker,false); }
    public SegmentBreaker(CellI cib,List<Point> points,CellType breaker, boolean includeBreakers)
    {
        this(cib,points,breaker,includeBreakers ? noop : skipBreakers);
    }
    public SegmentBreaker(CellI cib,List<Point> points,CellType breaker,boolean includeBreakers,IncludeI ...infilters)
    {
        this(cib,points,breaker,append(infilters,includeBreakers ? noop : skipBreakers));
    }

    public SegmentBreaker(CellI cib, List<Point> points,CellType breaker,IncludeI ...infilters)
    {
        List<Segment> tsegments = new ArrayList<>();

        CellType startct = cib.op(points.get(0),0);

        tsegments.add(new Segment(startct == breaker));

        for (int i = 0 ; i < points.size() ; ++i)
        {
            Point p = points.get(i);
            CellType ct = cib.op(p,i);
            Segment curSegment = tsegments.get(tsegments.size()-1);
            if (curSegment.isBreaker() && ct != breaker)
            {
                tsegments.add(curSegment = new Segment(false));
            }
            else if (!curSegment.isBreaker() && ct == breaker)
            {
                tsegments.add(curSegment = new Segment(true));
            }
            curSegment.add(p);
        }

        for (Segment s : tsegments) if (isIncluded(s,infilters)) segments.add(s);
    }
}
