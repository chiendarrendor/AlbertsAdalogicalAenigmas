import java.awt.*;

/**
 * Created by chien on 6/16/2017.
 */
public enum Direction
{
    NORTH(0,-1),
    SOUTH(0,1),
    EAST(1,0),
    WEST(-1,0);

    private int dx;
    private int dy;

    private Direction(int dx,int dy) { this.dx = dx; this.dy = dy;}
    public int dx() { return dx; }
    public int dy() { return dy; }
    public Point goDir(int x,int y) { return new Point(x+dx(),y+dy());}
    public Point goDir(Point p) { return goDir(p.x,p.y); }
}
