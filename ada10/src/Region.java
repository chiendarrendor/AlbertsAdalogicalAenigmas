import java.awt.*;
import java.util.Vector;

/**
 * Created by chien on 4/22/2017.
 */
public class Region
{
    private Vector<Point> cells = new Vector<Point>();
    private boolean hasNumber = false;
    private int number;
    StringBuffer codegenerator = new StringBuffer();

    public Region(char code) { codegenerator.append("Code ").append(code); }

    public void addCell(int x,int y) { cells.add(new Point(x,y)); codegenerator.append("(").append(x).append(",").append(y).append(")");}
    public void setNumber(int num) { hasNumber = true; number = num; codegenerator.append("(").append(num).append(")");}

    public boolean hasNumber() { return hasNumber; }
    public int getNumber() { return number; }
    public Vector<Point> getPoints() { return cells; }
    public String getCode() { return codegenerator.toString(); }

    public int TreeCount(Board b)
    {
        int result = 0;
        for (Point p : cells)
        {
            if (b.getCell(p.x,p.y) == CellType.TREE) ++result;
        }
        return result;
    }

}
