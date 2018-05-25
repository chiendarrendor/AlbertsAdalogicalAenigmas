import grid.graph.GridGraph;
import grid.logic.LogicStatus;
import grid.puzzlebits.Direction;

import java.awt.*;
import java.util.Set;

// this class explores the behavior of the regions of the grid that we
// know are connected by paths.
public class MinimumLogicStep extends CommonLogicStep {
    public MinimumLogicStep() { super(true); }
    @Override
    public LogicStatus applyToGroup(Board thing, Set<Point> cells, Set<Point> numbers,GridGraph gg) {
        LogicStatus result = LogicStatus.STYMIED;
        if (numbers.size() > 2) return LogicStatus.CONTRADICTION;

        // all cells of a group must be connected to each other.
        for (Point p : cells) {
            for (Direction d: Utility.OPDIRS) {
                Point np = d.delta(p,1);
                if (!cells.contains(np)) continue;
                EdgeState es = thing.getEdge(p.x,p.y,d);
                if (es == EdgeState.WALL) return LogicStatus.CONTRADICTION;
                if (es == EdgeState.UNKNOWN) {
                    result = LogicStatus.LOGICED;
                    thing.setEdge(p.x,p.y,d,EdgeState.PATH);
                }

            }
        }

        int maxsize;
        if (numbers.size() < 2) maxsize = thing.getMax() - 1;
        else {
            int pairmax = numbers.stream().mapToInt(it->thing.getNumber(it.x,it.y)).max().getAsInt();
            int pairmin = numbers.stream().mapToInt(it->thing.getNumber(it.x,it.y)).min().getAsInt();
            if ((pairmax - pairmin) < 2) return LogicStatus.CONTRADICTION;
            maxsize = pairmax - 1;
        }

        if (cells.size() > maxsize) return LogicStatus.CONTRADICTION;

        /* BlastOut is breathtakingly expensive!
        if (numbers.size() > 0) {
            BlastOut bo = new BlastOut(thing,gg,cells,numbers);
            if (numbers.size() == 1) {
                LogicStatus bostat = bo.singleNumberExtend(thing);
                if (bostat == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
                if (bostat == LogicStatus.LOGICED) result = LogicStatus.LOGICED;
            }
        }
        */

        return result;
    }
}
