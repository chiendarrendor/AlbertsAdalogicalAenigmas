import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import sun.rmi.runtime.Log;

import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LineLogicStep implements LogicStep<Board> {
    List<Point> line;
    public String toString() { return "LineLogicStep: " + line.get(0) + " " + line.get(1); }
    public LineLogicStep(List<Point> line) { this.line = line; }

    @Override public LogicStatus apply(Board thing) {
        Map<Integer,Set<Point>> ipoints = new HashMap<>();

        for (Point p : line) {
            CellSet cs = thing.getCellSet(p.x,p.y);
            if (cs.size() == 0) return LogicStatus.CONTRADICTION;
            for (int i : cs) {
                if (!ipoints.containsKey(i)) ipoints.put(i,new HashSet<Point>());
                ipoints.get(i).add(p);
            }
        }

        LogicStatus result = LogicStatus.STYMIED;

        for (int i = 1 ; i <= thing.getWidth(); ++i) {
            if (!ipoints.containsKey(i)) return LogicStatus.CONTRADICTION;
            if (ipoints.get(i).size() > 1) continue;
            Point tp = ipoints.get(i).iterator().next();
            CellSet cs = thing.getCellSet(tp.x,tp.y);

            if (cs.size() == 1) continue;
            result = LogicStatus.LOGICED;
            cs.is(i);
        }

        Map<Integer,Point> solos = new HashMap<>();
        for (Point p : line) {
            CellSet cs = thing.getCellSet(p.x,p.y);
            if (cs.size() > 1) continue;
            int num = cs.theNumber();
            if (solos.containsKey(num)) return LogicStatus.CONTRADICTION;
            solos.put(num,p);
        }

        for (int inum : solos.keySet()) {
            Point solo = solos.get(inum);
            for (Point p : line) {
                if (p.equals(solo)) continue;
                CellSet cs = thing.getCellSet(p.x,p.y);
                if (!cs.has(inum)) continue;
                result = LogicStatus.LOGICED;
                cs.isNot(inum);
            }
        }

        return result;
    }
}
