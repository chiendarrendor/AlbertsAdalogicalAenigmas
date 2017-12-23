import grid.logic.LogicStep;
import grid.logic.LogicStatus;

import java.util.Vector;

/**
 * Created by chien on 6/12/2017.
 */
public class IslandLogicStep implements LogicStep<Board>
{
    int x;
    int y;
    int count;

    public IslandLogicStep(int x, int y,int count)
    {
        this.x = x;
        this.y = y;
        this.count = count;
    }

    @Override
    public LogicStatus apply(Board b)
    {
        BoardCore thing = b.getBoardCore();
        int pathcount = 0;
        int blockcount = 0;
        Vector<Direction> opendirs = new Vector<Direction>();

        for (Direction dir : Direction.values())
        {
            if (thing.isBlocked(x,y,dir)) ++blockcount;
            else if (thing.isLinked(x,y,dir)) ++pathcount;
            else opendirs.add(dir);
        }

        if (pathcount > count) return LogicStatus.CONTRADICTION;
        if (pathcount + opendirs.size() < count) return LogicStatus.CONTRADICTION;
        if (opendirs.size() == 0)  return LogicStatus.STYMIED;

        if (pathcount == count)
        {
            for (Direction dir : opendirs)
            {
                thing.setBlock(x,y,dir);
            }
            return LogicStatus.LOGICED;
        }

        if (pathcount + opendirs.size() == count)
        {
            for (Direction dir : opendirs)
            {
                thing.setEdge(x,y,dir);
            }
            return LogicStatus.LOGICED;
        }
        return LogicStatus.STYMIED;
    }
}
