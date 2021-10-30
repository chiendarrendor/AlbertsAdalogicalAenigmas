import java.awt.Point;
import java.awt.geom.Line2D;

public class Segment {
    Point begin;
    Point end;

    public Segment(Point begin, Point end) { this.begin = begin; this.end = end; }

    public boolean intersects(Segment other) {
        return Line2D.linesIntersect(begin.x,begin.y,end.x,end.y,
                other.begin.x,other.begin.y,other.end.x,other.end.y);
    }
}
