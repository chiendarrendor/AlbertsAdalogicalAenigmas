package grid.puzzlebits;

import java.awt.*;

/**
 * Created by chien on 10/25/2017.
 */
public enum Turns
{
    LEFT,
    RIGHT,
    STRAIGHT;

    public static Turns makeTurn(Point from, Point through, Point to)
    {
        Direction d1 = Direction.fromTo(from.x,from.y,through.x,through.y);
        Direction d2 = Direction.fromTo(through.x,through.y,to.x,to.y);

        if (d1 == d2) return STRAIGHT;
        if (d1 == d2.getOpp()) throw new RuntimeException("This loops back!");

        switch(d1)
        {
            case EAST: return d2 == Direction.NORTH ? LEFT : RIGHT;
            case WEST: return d2 == Direction.NORTH ? RIGHT : LEFT;
            case NORTH: return d2 == Direction.WEST ? LEFT : RIGHT;
            case SOUTH: return d2 == Direction.WEST ? RIGHT : LEFT;
            default: throw new RuntimeException("Not orthogonal!");
        }
    }
}
