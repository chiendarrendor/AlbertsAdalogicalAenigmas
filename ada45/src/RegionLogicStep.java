import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import sun.rmi.runtime.Log;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/**
 * Created by chien on 7/1/2017.
 */
public class RegionLogicStep implements LogicStep<Board>
{
    char regionid;
    Set<Point> cells = new HashSet<>();
    boolean isNumbered;
    int size;


    public RegionLogicStep(Board b, char rid)
    {
        regionid = rid;
        for(int x = 0 ; x < b.getWidth() ; ++x)
        {
            for(int y = 0 ; y < b.getHeight() ; ++y)
            {
                if (!b.inRegion(x,y)) continue;
                if (b.getRegionId(x,y) != regionid) continue;
                cells.add(new Point(x,y));
            }
        }

        size = b.getRegionSize(regionid);
        isNumbered = size != -1;

    }

    // this class will process all hops of all rabbits
    // recording, for each rabbit that has at least one hop destined for this region
    // the hops that land on this region and the number of hops that don't.
    private class ImpingingRabbits
    {
        public class ImpingingRabbit
        {
            public int othercount() { return otherHops.size(); }
            Vector<Integer> otherHops = new Vector<>();
            Vector<Integer> impingingHops = new Vector<>();
            int rabid;
            int size;
            public ImpingingRabbit(int rabid,int size) { this.rabid = rabid ; this.size = size;}
        }

        public Vector<ImpingingRabbit> rabbits = new Vector<>();

        public ImpingingRabbits(Board b)
        {
            for (int i = 0 ; i < b.getNumRabbits() ; ++i)
            {
                ImpingingRabbit ir = new ImpingingRabbit(i,b.getRabbitSize(i));
                ir.rabid = i;
                Set<Integer> hops = b.getLiveHopSet(i);
                for (int hop : hops)
                {
                    RabbitHops.RabbitHop rh = b.hops.rabbits.elementAt(i).hops.elementAt(hop);
                    if (cells.contains(rh.cells.lastElement()))
                    {
                        ir.impingingHops.add(hop);
                    }
                    else
                    {
                        ir.otherHops.add(hop);
                    }
                }
                if (ir.impingingHops.size() > 0) rabbits.add(ir);
            }
        }
    }




    // logic:
    // 1) every numbered warren must have rabbits with that exact value that end there
    // 2) every warren must have at least one rabbit.

    @Override
    public LogicStatus apply(Board thing)
    {
        ImpingingRabbits myrabbits = new ImpingingRabbits(thing);
        LogicStatus result = LogicStatus.STYMIED;

        LogicStatus alo = atLeastOne(thing,myrabbits);
        if (alo == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
        if (alo == LogicStatus.LOGICED) result = LogicStatus.LOGICED;

        if (!isNumbered) return LogicStatus.STYMIED;

        int actualsum = 0;
        int possiblesum = 0;

        for (ImpingingRabbits.ImpingingRabbit rab : myrabbits.rabbits)
        {
            if (rab.othercount() == 0) actualsum += rab.size;
            else possiblesum += rab.size;
        }

        if (actualsum > size) return LogicStatus.CONTRADICTION;
        if (actualsum + possiblesum < size) return LogicStatus.CONTRADICTION;

        // if all actuals are together the right size, then all impinging
        // from all other sources must cease.
        if (actualsum == size)
        {
            for (ImpingingRabbits.ImpingingRabbit rab : myrabbits.rabbits)
            {
                // this _must_ be one of ours, so is okay.
                if (rab.othercount() == 0) continue;

                result = LogicStatus.LOGICED;
                for (int hopid : rab.impingingHops)
                {
                    thing.clearHop(rab.rabid,hopid, " already the right number of rabbits in warren");
                }
            }
        }

        // if all actuals plus all possibles are together the right size, all the possibles must
        // be modified so they are actuals.
        if (actualsum + possiblesum == size)
        {
            for (ImpingingRabbits.ImpingingRabbit rab : myrabbits.rabbits)
            {
                for (int hopid : rab.otherHops)
                {
                    result = LogicStatus.LOGICED;
                    thing.clearHop(rab.rabid,hopid, " this rabbit _must_ go to this warren");
                }
            }
        }

        return result;
    }


    private LogicStatus atLeastOne(Board thing, ImpingingRabbits myrabbits)
    {
        // if there are no rabbits that can even possibly land here, that's bad.
        if (myrabbits.rabbits.size() == 0) return LogicStatus.CONTRADICTION;
        // we can't make any logic if there are more than one
        if (myrabbits.rabbits.size() > 1) return LogicStatus.STYMIED;
        // so if we get here, we have exactly one rabbit.
        ImpingingRabbits.ImpingingRabbit rabbit = myrabbits.rabbits.elementAt(0);
        if (rabbit.impingingHops.size() > 1) return LogicStatus.STYMIED;
        // a rabbit _must_ have at least one impinging hop to be in the Impinging Rabbit list
        int impinginghop = rabbit.impingingHops.elementAt(0);
        if (rabbit.othercount() == 0) return LogicStatus.STYMIED;
        // if we get here, the other hops for this rabbit besides this one need to be removed.
        thing.setHop(rabbit.rabid, impinginghop, "this warren must have at least one rabbit");
        return LogicStatus.LOGICED;
    }
}
