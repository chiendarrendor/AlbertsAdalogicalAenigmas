/**
 * Created by chien on 5/18/2017.
 */
public class Clue
{
    int dist;
    Direction dir;
    int x;
    int y;

    public Clue(String s,int x,int y)
    {
        this.x = x;
        this.y = y;

        dist = Integer.parseInt(s.substring(0,1));

        switch(s.charAt(1))
        {
            case '^': dir = Direction.NORTH; break;
            case 'v': dir = Direction.SOUTH; break;
            case '<': dir = Direction.WEST; break;
            case '>': dir = Direction.EAST; break;
            default:
                throw new RuntimeException("Unknown direction char: " + s);
        }


    }

    public int getDist() { return dist;}
    public Direction getDir() { return dir; }
}
