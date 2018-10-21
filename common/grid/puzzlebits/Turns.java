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

    public static boolean isBend(Turns t) { return t == LEFT || t == RIGHT; }

    // given a direction of entry to the cell "into" the cell
    // what is the direction of exit?
    public Direction exitDir(Direction from) {
        if (this == STRAIGHT) return from;
        switch(from) {
            case NORTH: return this == RIGHT ? Direction.EAST : Direction.WEST;
            case SOUTH: return this == RIGHT ? Direction.WEST : Direction.EAST;
            case WEST: return this == RIGHT ? Direction.NORTH : Direction.SOUTH;
            case EAST: return this == RIGHT ? Direction.SOUTH : Direction.NORTH;
            default: throw new RuntimeException("Not orthogonal!");
        }
    }
}
