import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * Created by chien on 7/1/2017.
 */
public class RabbitLogicStep implements LogicStep<Board>
{
    RabbitHops.Rabbit myrabbit;


    public RabbitLogicStep(RabbitHops.Rabbit rab)
    {
        myrabbit = rab;
    }

    @Override

    // items of logic to apply:
    // 1. any hop  that has any isRabbit cells is invalid
    // 1a. invalidating all hops is a contradiction
    // 1b. if one hop is left after invalidating, all board cells must have rabbited ids equal to this rabbit
    // 1bI .. if board cells are not rabbited, make them so
    // 1bII .. if board cells are rabbited but not by us, contradiction
    // 2. any cell that is mentioned by _all_ non-invalid hops _must_ belong to this rabbit.

    public LogicStatus apply(Board thing)
    {
        LogicStatus result = LogicStatus.STYMIED;
        Set<Integer> hopids = thing.getLiveHopSet(myrabbit.id);

        for(int hopid : hopids.toArray(new Integer[0]))
        {
            if (!hopCellsValid(thing,hopid))
            {
                thing.clearHop(myrabbit.id,hopid," this hop impinges on another");
                result = LogicStatus.LOGICED;
                continue;
            }
        }
        if (hopids.size() == 0) return LogicStatus.CONTRADICTION;

        if (hopids.size() == 1)
        {
            int hopid = hopids.iterator().next();
            Vector<Point> hopcells = myrabbit.hops.elementAt(hopid).cells;
            for (Point p : hopcells)
            {
                if (!thing.isRabbited(p.x,p.y))
                {
                    thing.setRabbited(p.x,p.y,myrabbit.id);
                    result = LogicStatus.LOGICED;
                }
                else
                {
                    if (thing.getRabbited(p.x,p.y) != myrabbit.id) return LogicStatus.CONTRADICTION;
                }
            }
        }

        Map<Point,Integer> counters = new HashMap<>();
        for(int hopid : hopids.toArray(new Integer[0]))
        {
            Vector<Point> hopcells = myrabbit.hops.elementAt(hopid).cells;
            for (Point p : hopcells)
            {
                int ocount = counters.containsKey(p) ? counters.get(p) : 0;
                counters.put(p,ocount+1);
            }
        }

        for (Map.Entry<Point,Integer> ent : counters.entrySet())
        {
            if (ent.getValue() < hopids.size()) continue;
            Point p = ent.getKey();
            if (!thing.isRabbited(p.x,p.y))
            {
                thing.setRabbited(p.x,p.y,myrabbit.id);
                result = LogicStatus.LOGICED;
            }
            else
            {
                if (thing.getRabbited(p.x,p.y) != myrabbit.id) return LogicStatus.CONTRADICTION;
            }
        }

        return result;
    }

    private boolean hopCellsValid(Board b,int hopid)
    {
        Vector<Point> hopcells = myrabbit.hops.elementAt(hopid).cells;
        for (Point p : hopcells)
        {
            if (!b.isRabbited(p.x,p.y)) continue;
            if (b.getRabbited(p.x,p.y) == myrabbit.id) continue;
            // if we get here, we are rabbited, but it's not us. this is bad.
            return false;
        }
        return true;
    }




}
