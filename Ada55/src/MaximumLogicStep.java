import grid.graph.GridGraph;
import grid.logic.LogicStatus;

import java.awt.*;
import java.util.Set;

// this creates sections that are joined by both paths and
// possibilities, breaking only on walls.   This means that
// groups may consist of not-yet separated final groups,
// so assumptions cannot be made about which numbers are or are not together.
public class MaximumLogicStep extends CommonLogicStep {
    public MaximumLogicStep() { super(false); }


    @Override
    public LogicStatus applyToGroup(Board thing, Set<Point> cells, Set<Point> numbers,GridGraph gg) {
        if (numbers.size() == 0) return LogicStatus.CONTRADICTION;
        if (numbers.size() % 2 != 0) return LogicStatus.CONTRADICTION;
        if (numbers.size() > 2) return LogicStatus.STYMIED;

        int pairmax = numbers.stream().mapToInt(it->thing.getNumber(it.x,it.y)).max().getAsInt();
        int pairmin = numbers.stream().mapToInt(it->thing.getNumber(it.x,it.y)).min().getAsInt();

        if (cells.size() <= pairmin ) return LogicStatus.CONTRADICTION;
        if (cells.size() >= pairmax ) return LogicStatus.CONTRADICTION;

        return LogicStatus.STYMIED;
    }

}
