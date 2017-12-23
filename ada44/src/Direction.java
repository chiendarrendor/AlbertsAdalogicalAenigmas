/**
 * Created by chien on 6/7/2017.
 */
public enum Direction
{
    NORTH(0,-1),
    SOUTH(0,1),
    EAST(1,0),
    WEST(-1,0),
    NORTHEAST(1,-1),
    SOUTHEAST(1,1),
    NORTHWEST(-1,-1),
    SOUTHWEST(-1,1);

    private int dx;
    private int dy;
    public int DX() { return dx;}
    public int DY() { return dy;}

    Direction(int dx,int dy)
    {
        this.dx = dx;
        this.dy = dy;
    }

    public Direction getOpp()
    {
        switch(this)
        {
            case NORTH: return SOUTH;
            case SOUTH: return NORTH;
            case EAST: return WEST;
            case WEST: return EAST;
            case NORTHEAST: return SOUTHWEST;
            case SOUTHEAST: return NORTHWEST;
            case NORTHWEST: return SOUTHEAST;
            case SOUTHWEST: return NORTHWEST;
            default: throw new RuntimeException("Bwah?");
        }
    }
}
