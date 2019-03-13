import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RegionLogicStep implements LogicStep<Board> {
    Set<Point> region;
    public RegionLogicStep(Set<Point> region) { this.region = region; }

    @Override public LogicStatus apply(Board thing) {
        Map<Integer,Set<Point>> bynumbers = new HashMap<>();
        for (Point p : region) {
            CellSet cs = thing.getCellSet(p.x,p.y);
            cs.stream().forEach(num-> {
                if (!bynumbers.containsKey(num)) bynumbers.put(num,new HashSet<>());
                bynumbers.get(num).add(p);
            });
        }

        LogicStatus result = LogicStatus.STYMIED;
        for (int i = 1 ; i <= region.size() ; ++i ) {
            if (!bynumbers.containsKey(i)) return LogicStatus.CONTRADICTION;
            if (bynumbers.get(i).size() > 1) continue;
            Point singular = bynumbers.get(i).iterator().next();
            CellSet singcs = thing.getCellSet(singular.x,singular.y);
            if (singcs.isDone()) continue;
            result = LogicStatus.LOGICED;
            singcs.is(i);
        }

        Map<Integer,Point> singles = new HashMap<>();
        for (Point p : region) {
            CellSet cs = thing.getCellSet(p.x,p.y);
            if (!cs.isDone()) continue;
            int num = cs.theNumber();
            if (singles.containsKey(num)) return LogicStatus.CONTRADICTION;
            singles.put(num,p);
        }

        for (int num : singles.keySet()) {
            Point mypoint = singles.get(num);

            for (Point p : region) {
                if (mypoint == p) continue;
                CellSet cs = thing.getCellSet(p.x,p.y);
                if (!cs.has(num)) continue;
                result = LogicStatus.LOGICED;
                cs.isNot(num);
            }
        }

        return result;
    }
}
