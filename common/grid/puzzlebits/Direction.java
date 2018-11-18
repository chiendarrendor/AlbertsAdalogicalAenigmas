package grid.puzzlebits;

import java.awt.*;
import java.util.List;

/**
 * Created by chien on 6/7/2017.
 */
public enum Direction
{
    NORTH(0,-1,"N",'↑'),
    NORTHEAST(1,-1,"NE",'↗'),
    EAST(1,0,"E",'→'),
    SOUTHEAST(1,1,"SE",'↘'),
    SOUTH(0,1,"S",'↓'),
    SOUTHWEST(-1,1,"SW",'↙'),
    WEST(-1,0,"W",'←'),
    NORTHWEST(-1,-1,"NW",'↖');


    private int dx;
    private int dy;
    private String shorthand;
    private char symbol;
    public int DX() { return dx;}
    public int DY() { return dy;}
    public String getShort() { return shorthand; }
    public char getSymbol() { return symbol; }
    public Point delta(Point p,int dist) { return new Point(p.x+dx*dist,p.y+dy*dist); }

    public static Direction[] orthogonals()
    {
        return new Direction[] {NORTH,SOUTH,EAST,WEST};
    }
    public static Direction[] diagonals() { return new Direction[] { NORTHEAST,NORTHWEST,SOUTHEAST,SOUTHWEST}; }
    public static Direction fromShort(String s)
    {
        for (Direction d : values())
        {
            if (d.getShort().equals((s))) return d;
        }
        throw new RuntimeException("Can't get direction from short " + s);
    }

    Direction(int dx,int dy,String shorthand,char symbol)
    {
        this.dx = dx;
        this.dy = dy;
        this.shorthand = shorthand;
        this.symbol = symbol;
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

    public Direction right() {
        switch(this) {
            case NORTH: return EAST;
            case NORTHEAST: return SOUTHEAST;
            case EAST: return SOUTH;
            case SOUTHEAST: return SOUTHWEST;
            case SOUTH: return WEST;
            case SOUTHWEST: return NORTHWEST;
            case WEST: return NORTH;
            case NORTHWEST: return NORTHEAST;
            default: throw new RuntimeException("Bwah?");
        }
    }

    public Direction clockwise() {
        switch(this) {
            case NORTH: return NORTHEAST;
            case NORTHEAST: return EAST;
            case EAST: return SOUTHEAST;
            case SOUTHEAST: return SOUTH;
            case SOUTH: return SOUTHWEST;
            case SOUTHWEST: return WEST;
            case WEST: return NORTHWEST;
            case NORTHWEST: return NORTH;
            default: throw new RuntimeException("how?");
        }
    }

    public Direction counterclockwise() {
        switch(this) {
            case NORTH: return NORTHWEST;
            case NORTHEAST: return NORTH;
            case EAST: return NORTHEAST;
            case SOUTHEAST: return EAST;
            case SOUTH: return SOUTHEAST;
            case SOUTHWEST: return SOUTH;
            case WEST: return SOUTHWEST;
            case NORTHWEST: return WEST;
            default: throw new RuntimeException("why?");
        }
    }



    public static Direction fromTo(int x1, int y1, int x2, int y2)
    {
        for (Direction d : Direction.values())
        {
            if (x2 == x1 + d.DX() && y2 == y1 + d.DY()) return d;
        }
        throw new RuntimeException("these two points are not adjacent!");
    }
}
