import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

public class RectangleRegionHandler {
    Set<RectangleRegion> originalRectangles = new HashSet<>();
    Set<RectangleRegion> currentRectangles = new HashSet<>();
    Realness realness;


    public RectangleRegionHandler(Grid g, int size, Point center, Realness initialrealness) {
        realness = initialrealness;

        Set<Point> sizes = new HashSet<>();
        for (int width = 1 ; width <= size ; ++width) {
            for (int height = 1 ; height <= size ; ++height) {
                if (width * height != size) continue;
                sizes.add(new Point(width,height));
            }
        }

        for (Point p : sizes) {
            for (int x = 0 ; x < p.x ; ++x) {
                for (int y = 0 ; y < p.y ; ++y) {
                    RectangleRegion rr = new RectangleRegion(center,p,new Point(x,y));
                    if (rr.onBoard(g)) {
                        originalRectangles.add(rr);
                        currentRectangles.add(rr);
                        rr.addToGrid(g);
                    }
                }
            }
        }
    }

    public RectangleRegionHandler(RectangleRegionHandler right) {
        this.originalRectangles = right.originalRectangles;
        this.currentRectangles.addAll(right.currentRectangles);
        this.realness = right.realness;
    }

    public boolean hasRectangle(RectangleRegion rr) { return currentRectangles.contains(rr); }
    public void remove(RectangleRegion rr,Grid g) {
        currentRectangles.remove(rr);
        rr.removeFromGrid(g);
    }

    public void removeAll(Grid g) {
        for (RectangleRegion rr : currentRectangles) rr.removeFromGrid(g);
        currentRectangles.clear();
    }


    public void set(RectangleRegion rr,Grid g) {
        if (!hasRectangle(rr)) throw new RuntimeException("Can't call set on a missing rectangle!");
        Set<RectangleRegion> temp = new HashSet<RectangleRegion>(currentRectangles);
        for (RectangleRegion trr : temp) {
            if (rr == trr) continue;
            remove(trr,g);
        }
    }


    public void removeAllExcept(Grid g, Set<RectangleRegion> rrs) {
        Set<RectangleRegion> temp = new HashSet<>(currentRectangles);
        for (RectangleRegion trr : temp) {
            if (rrs.contains(trr)) continue;
            remove(trr,g);
        }
    }
}
