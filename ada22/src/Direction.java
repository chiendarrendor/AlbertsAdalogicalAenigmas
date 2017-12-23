/**
 * Created by chien on 5/18/2017.
 */
public enum Direction
{
    NORTH('↑',0,-1),
    SOUTH('↓',0,1),
    EAST('→',1,0),
    WEST('←',-1,0);

    private char arrow;
    private int dx;
    private int dy;
    Direction(char arrow,int dx,int dy)
    {
        this.arrow = arrow;
        this.dx = dx;
        this.dy = dy;
    }
    public char getArrow() { return arrow; }
    public int dx() { return dx;}
    public int dy() { return dy;}
}
