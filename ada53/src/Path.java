import grid.puzzlebits.Direction;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class Path {
    Point initial;
    Point terminal;
    Direction dir = null;
    // this will contain all points not initial;
    Set<Point> pathPoints = new HashSet<>();
    int dist;

    public Path(Point p,int dist) {
        this.dist = dist;
        initial = terminal = p;
    }

    public Path(Path right) {
        initial = right.initial;
        terminal = right.terminal;
        dir = right.dir;
        pathPoints.addAll(right.pathPoints);
        dist = right.dist;
    }

    // we are going to trust to our superiors that the points they give us are on a line.
    public void addPoint(Point p) {
        if (dir == null)   dir = Direction.fromTo(initial.x,initial.y,p.x,p.y);
        terminal = p;
        pathPoints.add(p);
    }

    public Point getInitial() { return initial; }
    public int getLength() { return pathPoints.size(); }
    public Stream<Point> stream() { return pathPoints.stream(); }
    public int getDist() { return dist; }


    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(initial);
        sb.append("->");
        sb.append("(");
        pathPoints.stream().forEach((x)->sb.append(x));
        sb.append(")");
        sb.append("->");
        sb.append(terminal);
        return sb.toString();
    }
}
