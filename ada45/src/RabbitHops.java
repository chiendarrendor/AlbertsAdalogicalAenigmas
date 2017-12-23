import grid.puzzlebits.Direction;

import java.awt.*;
import java.util.*;

/**
 * Created by chien on 6/30/2017.
 */

    // This class will uniquely identify each rabbit, and then
    // calculate the possible destinations the rabbit can make thus:
    // a) rabbits move in straight lines, N,S,E,W
    // b) they may go no further than the edge of the board or another rabbit
    // c) they may only stop on a warren (region = true) space
    // d) they may only stop on the _first_ space of a given region they see.

    // ultimately, the goal of this puzzle is for exactly one hop to be chosen for each rabbit,
    // but this class is not responsible for those choices.

public class RabbitHops
{
    Vector<Rabbit> rabbits = new Vector<>();
    Map<Point,Rabbit> rabbitLocations = new HashMap<>();

    public class RabbitHop
    {
        int id;
        Direction dir;
        Vector<Point> cells = new Vector<>();
        public RabbitHop(int id,Direction dir) { this.id = id; this.dir = dir; }
    }

    public class Rabbit
    {
        int id;
        int sx;
        int sy;
        Vector<RabbitHop> hops = new Vector<>();

        public Rabbit(int size, int x, int y)
        {
            id  = size;
            sx = x;
            sy = y;
        }
    }

    public RabbitHops(Board b)
    {
        for(int x = 0 ; x < b.getWidth() ; ++x)
        {
            for (int y = 0 ; y < b.getHeight() ; ++y)
            {
                if (!b.isRabbit(x,y)) continue;

                Rabbit r = new Rabbit(rabbits.size(),x,y);
                for (Direction dir : Direction.orthogonals())
                {
                    TestHop(b,r,dir);
                }
                rabbits.add(r);
                rabbitLocations.put(new Point(x,y),r);
            }
        }
    }

    private void TestHop(Board b,Rabbit r, Direction dir)
    {
        Vector<Point> hopPoints = new Vector<>();
        Set<Character> seenRegions = new HashSet<>();

        Point curp = new Point(r.sx,r.sy);

        // invariant...curp is done and handled. lets make the next step.
        while(true)
        {
            curp = new Point(curp.x + dir.DX(),curp.y + dir.DY());
            if (!b.inBounds(curp)) return;
            if (b.isRabbit(curp.x,curp.y)) return;
            // if we are here, this space is either an empty space or a warren.
            hopPoints.add(curp);
            if (!b.inRegion(curp.x,curp.y)) continue;
            // so we are in a region.   if we've seen this region before, we can't stop
            if (seenRegions.contains(b.getRegionId(curp.x,curp.y))) continue;
            // we make a hop!
            seenRegions.add(b.getRegionId(curp.x,curp.y));
            RabbitHop hop = new RabbitHop(r.hops.size(),dir);
            hop.cells.addAll(hopPoints);
            r.hops.add(hop);
        }
    }

    public void show()
    {
        for (Rabbit rab : rabbits)
        {
            System.out.println("Rabbit # " + rab.id + " starting at (" +rab.sx + "," + rab.sy + ")");
            for (RabbitHop hop : rab.hops)
            {
                System.out.print("\thop #id " + hop.id + ":");
                for (Point p : hop.cells)
                {
                    System.out.print(" (" + p.x + "," + p.y + ")");
                }
                System.out.println("");
            }
        }
    }


}
