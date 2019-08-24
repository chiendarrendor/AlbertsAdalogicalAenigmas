import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

public class RectangleRegion {
    Point realCenter;
    Point dimensions;
    Point offsetCenter;
    Point upperleft;
    Set<Point> realPoints = new HashSet<>();

    public RectangleRegion(Point realCenter,Point dimensions,Point offsetCenter) {
        this.realCenter = realCenter;
        this.dimensions = dimensions;
        this.offsetCenter = offsetCenter;

        for (int x = 0 ; x < dimensions.x ; ++x) {
            for (int y = 0 ; y < dimensions.y ; ++y) {
                realPoints.add(new Point(x+realCenter.x-offsetCenter.x,y+realCenter.y-offsetCenter.y));
            }
        }
        upperleft = new Point(realCenter.x-offsetCenter.x,realCenter.y-offsetCenter.y);
    }

    //
    // real center = 4,8
    // dimensions = 3,4
    // offset center = 0,1
    //
    // # = region cells
    // @ = offset center
    //
    //      456
    //    7 ###
    //    8 @##
    //    9 ###
    //   10 ###
    //
    // region offset x coordinate 0 -> real x coordinate 4
    // region offset y coordinate 3 -> real y coordinate 10
    // real = offset + realcenter - offsetcenter
    //   10 = 3        8            1
    //    4 = 0        4            0


    public boolean onBoard(Grid g) {
        if (g == null) throw new RuntimeException("Can't onBoard with null Grid");
        for (Point p : realPoints) {
            if (p == null) throw new RuntimeException("Why is there a null Point in realPoints?");
            if (!g.inBounds(p)) return false;
        }
        return true;
    }

    public String toString() {
        return ""
                + new Point(0 + realCenter.x - offsetCenter.x, 0 + realCenter.y - offsetCenter.y)
                + new Point(dimensions.x - 1 + realCenter.x - offsetCenter.x, dimensions.y - 1 + realCenter.y - offsetCenter.y)
                + "(" + dimensions + ")";
    }

    public void addToGrid(Grid g) {
        for (Point p : realPoints) g.addToGrid(p.x,p.y,this);
    }

    public void removeFromGrid(Grid g) {
        for (Point p : realPoints) g.removeFromGrid(p.x,p.y,this);
    }
}
