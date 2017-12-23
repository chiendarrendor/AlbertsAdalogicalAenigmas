import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import sun.misc.Perf;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by chien on 10/14/2017.
 */
public class RegionLogicStep implements LogicStep<Board>
{
    char rid;
    public RegionLogicStep(char rid) { this.rid = rid;}


    @Override
    public LogicStatus apply(Board thing)
    {
        Region reg = thing.getRegion(rid);
        Set<Point> mylocks = new HashSet<>();
        Set<Rectangle> inboundRegions = new HashSet<>();
        Set<Rectangle> deletions = new HashSet<>();
        LogicStatus result = LogicStatus.STYMIED;

        // any rectangle containing cells that do not contain this region must be scrapped.
        for (Rectangle r : reg.possibles)
        {
            boolean[] hasmissing = new boolean[1];
            hasmissing[0] = false;

            thing.forEachRectangleCell(r,(x,y)->{
                Cell c = thing.getCell(x,y);
                if(c.isFixed() && c.getPossibles().iterator().next() == rid) mylocks.add(new Point(x,y));

                if (!c.canBe(rid)) hasmissing[0] = true;
                return true;
            });

            if (hasmissing[0] == false) inboundRegions.add(r);
            else
            {
                deletions.add(r);
                result = LogicStatus.LOGICED;
            }
        }
        reg.possibles = inboundRegions;

        // any rectangle _not_ containing cells that are LOCKED to this region must be scrapped.
        Set<Rectangle> lockedContainers = new HashSet<>();
        for (Rectangle r: reg.possibles)
        {
            boolean missinglock = false;
            for(Point p : mylocks)
            {
                if (!r.contains(p))
                {
                    missinglock = true;
                    break;
                }
            }
            if (!missinglock) lockedContainers.add(r);
            else
            {
                deletions.add(r);
                result = LogicStatus.LOGICED;
            }
        }
        reg.possibles = lockedContainers;

        // zero rectangles -> contradiction
        if (reg.possibles.size() == 0) return LogicStatus.CONTRADICTION;

        // otherwise, intersection of all remaining rectangles must be LOCKED to this region
        Rectangle intersection  = null;

        for(Rectangle r : reg.possibles)
        {
            if (intersection == null) intersection = r;
            else intersection = intersection.intersection(r);
        }

        boolean[] changed = new boolean[1];
        changed[0] = false;

        thing.forEachRectangleCell(intersection,(x,y)->{
            Cell c = thing.getCell(x,y);
            if (c.isFixed()) return true;
            c.setIs(rid);
            changed[0] = true;
            return true;
        });

        if (changed[0]) result = LogicStatus.LOGICED;

        // finally, any cell that is only in a deleted rectangle must be removed from this region.
        for (Rectangle doomed : deletions)
        {
            thing.forEachRectangleCell(doomed,(x,y)->{
                if (isInPossible(reg,x,y)) return true;
                thing.getCell(x,y).setImpossible(rid);
                return true;
            });
        }






        return result;
    }

    private boolean isInPossible(Region r, int x, int y)
    {
        for (Rectangle rec : r.possibles)
        {
            if (rec.contains(x,y)) return true;
        }
        return false;
    }


}
