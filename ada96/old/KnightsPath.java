import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KnightsPath {
    private static int nextId = 0;
    private int id;

    List<Point> locations = new ArrayList<>();
    SegmentSet segments = new SegmentSet();

    public KnightsPath(Point startingLocation) {
        id = ++nextId;
        locations.add(startingLocation);
    }

    public KnightsPath(KnightsPath right) {
        id = ++nextId;
        locations.addAll(right.locations);
        segments = new SegmentSet(right.segments);
    }

    public Point tail() {
        return locations.get(locations.size()-1);
    }

    public boolean addable(Point p) {
        Segment s = new Segment(tail(),p);
        return segments.addable(s);
    }

    public void add(Point p) {
        Segment s = new Segment(tail(),p);
        locations.add(p);
        segments.addSegment(s);
    }


    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("KnightsPath: ").append(locations.get(0));
        for (int i = 1 ; i < locations.size() ; ++i) sb.append("->").append(locations.get(i));
        return sb.toString();
    }

    public int getId() { return id; }
    public int size() { return locations.size(); }
    public Collection<Point> getLocations() { return locations; }
}
