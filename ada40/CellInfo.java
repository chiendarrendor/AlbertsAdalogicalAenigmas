/**
 * Created by chien on 2/19/2017.
 */
public class CellInfo
{
    public static final int UNKNOWN = 0;
    public static final int RECTANGLE = 1;
    public static final int WALL = 2;
    public static final int EMPTY = 3; // empty is known to be not a wall, but unknown as to which rectangle

    int type;
    int rectnum;
    int x;
    int y;

    public CellInfo(int type, int rectnum, int x, int y)
    {
        this.type = type;
        this.rectnum = rectnum;
        this.x = x;
        this.y = y;
    }

    public CellInfo(CellInfo right)
    {
        type = right.type;
        rectnum = right.rectnum;
        x = right.x;
        y = right.y;
    }
}
