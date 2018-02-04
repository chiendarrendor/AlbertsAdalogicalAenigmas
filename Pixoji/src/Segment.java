import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Segment
{
    private boolean isBreaker;
    private List<Point> points = new ArrayList<Point>();


    public boolean isBreaker() { return isBreaker; }
    public Segment(boolean isBreaker) {  this.isBreaker = isBreaker; }
    public void add(Point p) { points.add(p); }
    public int size()
    {
        return points.size();
    }
    public int weight(Board b) { return points.stream().mapToInt((p)->{ return b.getWeight(p.x,p.y); }).sum();}
    public List<Point> getPoints() { return points; }
}
