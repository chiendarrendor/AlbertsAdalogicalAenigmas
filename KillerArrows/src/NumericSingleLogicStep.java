import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

public class NumericSingleLogicStep implements LogicStep<Board> {
    List<Point> cells;

    public NumericSingleLogicStep(List<Point> cells) { this.cells = cells; }

    @Override public LogicStatus apply(Board thing) {
        LogicStatus result = LogicStatus.STYMIED;

        // Any cell that is known means no other cell can have that value
        // also, if two cells are known with the same value, fail.
        Map<Integer,Point> knowns = new HashMap<>();
        for (Point p : cells) {
            Cell c = thing.getCell(p.x,p.y);
            if (!c.isValid()) return LogicStatus.CONTRADICTION;
            if (!c.isDone()) continue;
            int v = c.getValue();
            if (knowns.containsKey(v)) return LogicStatus.CONTRADICTION;
            knowns.put(v,p);
        }

        // if we get here, we know that all cells have at least 1 value, and no two cells that are known share their value.
        // We can't invalidate any cells by this next step.
        for (int v : knowns.keySet()) {
            Point acting = knowns.get(v);
            for (Point p : cells) {
                if (p.equals(acting)) continue;
                Cell c = thing.getCell(p.x,p.y);
                if (!c.has(v)) continue;
                c.clear(v);
                result = LogicStatus.LOGICED;
            }
        }

        // part 2.   each number must be known at least once
        // step 1.   find out what cells can possibly know what numbers
        Map<Integer,List<Point>> numbers = new HashMap<>();
        IntStream.rangeClosed(1,9).forEach(i->numbers.put(i,new ArrayList<>()));

        for (Point p : cells) {
            Cell c = thing.getCell(p.x,p.y);
            for (int v : c.contents()) {
                numbers.get(v).add(p);
            }
        }

        // part 2.
        // * if any number is known 0 times, contradiction,
        // * 1 times is 'Solo'
        for (int v : numbers.keySet()) {
            List<Point> vlist = numbers.get(v);
            if (vlist.size() == 0) return LogicStatus.CONTRADICTION;
            if (vlist.size() > 1) continue;

            Point theP = vlist.get(0);
            Cell c = thing.getCell(theP.x,theP.y);

            // this next item would only happen if the same cell ended up solo for two numbers
            if (!c.has(v)) return LogicStatus.CONTRADICTION;
            if (c.isDone()) continue;
            result = LogicStatus.LOGICED;
            c.set(v);
        }

        return result;
    }
}
