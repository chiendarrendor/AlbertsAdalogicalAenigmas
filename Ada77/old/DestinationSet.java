import java.awt.Point;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class DestinationSet {
    private Set<Point> destinations = new HashSet<>();

    public DestinationSet(Collection<Point> points) { destinations.addAll(points); }
    public DestinationSet(DestinationSet right) { destinations.addAll(right.destinations); }
    public boolean has(Point p) { return destinations.contains(p); }
    public boolean set(Point p) {
        if (!has(p)) return false;
        destinations.clear();
        destinations.add(p);
        return true;
    }
    public void remove(Point p) { destinations.remove(p); }
}
