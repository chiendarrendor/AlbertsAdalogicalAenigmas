import grid.file.GridFileReader;
import grid.logic.flatten.FlattenSolvable;
import grid.logic.flatten.FlattenSolvableTuple;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by chien on 6/30/2017.
 */
public class Board implements FlattenSolvable<Board>
{
    GridFileReader gfr;
    RabbitHops hops;
    private Vector<Set<Integer>> liveHops = new Vector<Set<Integer>>();

    // if >= 0, that particular rabbit has been here.  -1 means no rabbit has been here.
    int[][] rabbited;
    Set<Character> regionids = new HashSet<>();

    public Board (String fname)
    {
        gfr = new GridFileReader(fname);
        hops = new RabbitHops(this);
        hops.show();
        rabbited = new int[getWidth()][getHeight()];

        for (int x = 0 ; x < getWidth() ; ++x)
        {
            for (int y = 0 ; y < getHeight() ; ++y)
            {
                rabbited[x][y] = isRabbit(x,y) ? hops.rabbitLocations.get(new Point(x,y)).id : -1;
                if (inRegion(x,y)) regionids.add(getRegionId(x,y));
            }
        }


        for (int rid = 0 ; rid < hops.rabbits.size() ; ++rid)
        {
            Set<Integer> hids  = new HashSet<>();
            for (int hid = 0 ; hid < hops.rabbits.elementAt(rid).hops.size() ; ++hid )
            {
                hids.add(hid);
            }
            liveHops.add(hids);
        }
    }

    public Board(Board right)
    {
        gfr = right.gfr;
        hops = right.hops;
        regionids = right.regionids;
        rabbited = new int[getWidth()][getHeight()];

        for (Set<Integer> set : right.liveHops)
        {
            Set<Integer> newset = new HashSet<>();
            newset.addAll(set);
            liveHops.add(newset);
        }

        for (int x = 0 ; x < getWidth() ; ++x)
        {
            for (int y = 0 ; y < getHeight() ; ++y)
            {
                rabbited[x][y] = right.rabbited[x][y];
            }
        }

    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public boolean inBounds(int x,int y) { return gfr.inBounds(x,y); }
    public boolean inBounds(Point p) { return gfr.inBounds(p); }

    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0);}
    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.'; }

    public char getRegionId(int x,int y) {return gfr.getBlock("REGIONS")[x][y].charAt(0);}
    public boolean inRegion(int x,int y) { return getRegionId(x,y) != '.';}
    public int getRegionSize(char regionId)
    {
        String s = gfr.getVar("" + regionId);
        if (s == null) return -1;
        return Integer.parseInt(s);
    }

    public boolean isRabbit(int x,int y)
    {
        return gfr.getBlock("RABBITS")[x][y].charAt(0) != '.';
    }

    public int getRabbitSize(int x,int y)
    {
        return Integer.parseInt(gfr.getBlock("RABBITS")[x][y]);
    }

    public int getRabbitSize(int rabid)
    {
        RabbitHops.Rabbit rab = hops.rabbits.elementAt(rabid);
        return getRabbitSize(rab.sx,rab.sy);
    }


    public Set<Integer> getLiveHopSet(int rabid)
    {
        return liveHops.elementAt(rabid);
    }

    public int getNumRabbits() { return liveHops.size(); }


    public void clearHop(int rabidx,int hopidx,String why)
    {
//        RabbitHops.Rabbit rabbit = hops.rabbits.elementAt(rabidx);
 //       System.out.println("Rabbit #" + rabidx + " at (" + rabbit.sx + "," + rabbit.sy + ") hop " + hopidx + " cleared: " + why );


        liveHops.elementAt(rabidx).remove(hopidx);
    }

    public void setHop(int rabidx,int hopidx,String why)
    {
 //       RabbitHops.Rabbit rabbit = hops.rabbits.elementAt(rabidx);
 //       System.out.println("Rabbit #" + rabidx + " at (" + rabbit.sx + "," + rabbit.sy + ") hop " + hopidx + " cleared: " + why);

        liveHops.elementAt(rabidx).clear();
        liveHops.elementAt(rabidx).add(hopidx);
    }

    boolean isRabbited(int x,int y) { return rabbited[x][y] != -1; }
    int getRabbited(int x,int y) { return rabbited[x][y]; }
    void setRabbited(int x,int y,int rabid) { rabbited[x][y] = rabid; }




    @Override
    public boolean isComplete()
    {
        for(Set<Integer> hopset : liveHops)
        {
            if (hopset.size() > 1) return false;
        }
        return true;
    }

    private class Move
    {
        int rabbitidx;
        int hoptoskip;
        public Move(int r,int h) { rabbitidx = r ; hoptoskip = h; }
    }

    private FlattenSolvableTuple getOneTuple(int rabidx)
    {
        Set<Integer> hopids = liveHops.elementAt(rabidx);
        if (hopids.size() < 2) return null;
        FlattenSolvableTuple fst = new FlattenSolvableTuple();

        for (int hopid : hopids)
        {
            Board b = new Board(this);
            b.setHop(rabidx,hopid,"a guess");
            Move m = new Move(rabidx,hopid);
            fst.addTuple(b,m);
        }
        return fst;
    }

    @Override
    public List<FlattenSolvableTuple<Board>> getSuccessorTuples()
    {
        Vector<FlattenSolvableTuple<Board>> successors = new Vector<>();
        for (int i = 0 ; i < liveHops.size() ; ++i)
        {
            FlattenSolvableTuple fst = getOneTuple(i);
            if (fst == null) continue;
            successors.add(fst);
        }
        return successors;
    }

    @Override
    public void applyMove(Object o)
    {
        Move m = (Move)o;
        clearHop(m.rabbitidx,m.hoptoskip," a contradiction from a flatten");
    }

    @Override
    public List<Board> guessAlternatives()
    {
        Vector<Board> successors = new Vector<>();
        for (int i = 0 ; i < liveHops.size() ; ++i)
        {
            FlattenSolvableTuple fst = getOneTuple(i);
            if (fst == null) continue;
            return fst.choices;
        }
        throw new RuntimeException("guessAlternatives should only be called if a guess is possible!");
    }
}
