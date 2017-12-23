import grid.puzzlebits.Direction;

import java.awt.*;
import java.util.List;
import java.util.Vector;

/**
 * Created by chien on 10/18/2017.
 */
public class Region
{
    public class EdgeTuple
    {
        int x;
        int y;
        Direction d;
        public EdgeTuple(int x,int y,Direction d) { this.x = x ; this.y = y; this.d = d; }
    }
    public Vector<Point> cells = new Vector<>();
    public Vector<EdgeTuple> edges = new Vector<>();

    public List<EdgeTuple> getEdgeTuples() { return edges; }
    public List<Point> getCells() { return cells; }
    public void addCell(int x, int y) { cells.add(new Point(x,y)); }
    public void addEdge(int x, int y, Direction d) { edges.add(new EdgeTuple(x,y,d)); }
}
