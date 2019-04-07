import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

public class Region {
    private char regionId;
    private Set<Point> suns = new HashSet<>();
    private Set<Point> moons = new HashSet<>();
    private Set<Point> voids = new HashSet<>();
    private Set<Point> all = new HashSet<>();
    public Region(char rid) { regionId = rid; }
    public void addSun(Point p) { suns.add(p); all.add(p); }
    public void addMoon(Point p) { moons.add(p); all.add(p); }
    public void addVoid(Point p) { voids.add(p); all.add(p); }
    public Set<Point> getCells() { return all; }
    public char getId() { return regionId; }
}
