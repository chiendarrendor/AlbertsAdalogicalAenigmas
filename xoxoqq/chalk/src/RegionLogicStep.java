import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import sun.rmi.runtime.Log;

import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RegionLogicStep implements LogicStep<Board> {
    char id;
    public RegionLogicStep(char id) { this.id = id; }

    @Override public LogicStatus apply(Board thing) {
        Set<Point> rcells = thing.getRegion(id).cells;
        int rsize = rcells.size();

        Map<Integer,Set<Point>> numholder = new HashMap<>();

        for (Point p : rcells) {
            for(int num : thing.getCellSet(p.x,p.y)) {
                if (!numholder.containsKey(num)) numholder.put(num, new HashSet<Point>());
                numholder.get(num).add(p);
            }
        }

        LogicStatus result = LogicStatus.STYMIED;
        for (int idx = 1 ; idx <= rsize ; ++idx) {
            // at least one cell of a region should be able to contain each number.
            if (!numholder.containsKey(idx)) return LogicStatus.CONTRADICTION;
            Set<Point> numcells = numholder.get(idx);

            if (numcells.size() > 1) continue;
            Point numpoint = numcells.iterator().next();
            CellSet numcellset = thing.getCellSet(numpoint.x,numpoint.y);
            if (!numcellset.has(idx)) return LogicStatus.CONTRADICTION;
            if (numcellset.size() == 1) continue;
            result = LogicStatus.LOGICED;
            numcellset.is(idx);
        }

        Map<Integer,Point> singles = new HashMap<>();
        for (Point p : rcells) {
            CellSet pcellset = thing.getCellSet(p.x,p.y);
            if (pcellset.size() != 1) continue;
            int pnum = pcellset.theNumber();
            if (singles.containsKey(pnum)) return LogicStatus.CONTRADICTION;
            singles.put(pnum,p);
        }

        for (Point p : rcells) {
            CellSet pcellset = thing.getCellSet(p.x,p.y);

            for (int num : singles.keySet()) {
                if (p.equals(singles.get(num))) continue;
                if (!pcellset.has(num)) continue;
                result = LogicStatus.LOGICED;
                pcellset.isNot(num);
            }
        }


        return result;
    }
}
