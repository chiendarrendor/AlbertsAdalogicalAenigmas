import java.awt.*;
import java.util.Vector;

/**
 * Created by chien on 5/6/2017.
 */
public class Region
{
    char id;
    Vector<Point> cells = new Vector<>();

    public Region(char id)
    {
        this.id = id;
    }

    public void addCell(int x, int y)
    {
        cells.add(new Point(x,y));
    }

    public char getId() { return id;}
    public Vector<Point> getCells() { return cells; }
}
