import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UniqueLogicStep implements LogicStep<Board> {
    List<Point> cells;
    public UniqueLogicStep(List<Point> cells) { this.cells = cells; }

    @Override public LogicStatus apply(Board thing) {

        // if any cell has no entries in it, contradiction.
        // if any cell is a numeric solo, no other cell has that value
        // if two cells are both black (value 0), no other cell will be black

        LogicStatus result = LogicStatus.STYMIED;

        Point blackone = null;
        for (Point p : cells) {
            CellSet cs = thing.getCell(p.x,p.y);
            if (cs.count() == 0) return LogicStatus.CONTRADICTION;
            if (!cs.isSolo()) continue;
            int mysolo = cs.getSolo();
            Set<Point> ignores = new HashSet<>();
            if (mysolo == CellSet.BLACK) {
                if (blackone == null) {
                    blackone = p;
                    continue;
                }
                // if we get here, we have two blacks.
                ignores.add(blackone);
                ignores.add(p);
            } else {
                ignores.add(p);
            }
            // if we get here, we've found a solo (or a dual-solo black)
            for (Point op : cells) {
                if (ignores.contains(op)) continue;
                CellSet opcs = thing.getCell(op.x,op.y);
                if (!opcs.has(mysolo)) continue;
                // we know it has the one we want to remove...if that's the only one, that's bad.
                if (opcs.isSolo()) return LogicStatus.CONTRADICTION;
                opcs.remove(mysolo);
                result = LogicStatus.LOGICED;
            }
        }
        // if we survive to here, every cell has been looked at least once for emptiness.
        // and we also validated that we didn't empty a cell during the remove process.

        // if any value is present only once (twice for black), that cell (those cells) must solo that value
        // if a value is not present at all, contradiction.

        Map<Integer,List<Point>> bynumber = new HashMap<Integer,List<Point>>();

        for (Point p : cells) {
            CellSet cs = thing.getCell(p.x,p.y);
            if (cs.count() == 0) return LogicStatus.CONTRADICTION;
            for (int v : cs) {
                if (!bynumber.containsKey(v)) {
                    bynumber.put(v,new ArrayList<>());
                }
                bynumber.get(v).add(p);
            }
        }

        for (int v = 0 ; v <= thing.maxnum ; ++v) {
            List<Point> vpoints = bynumber.get(v);
            if (vpoints == null) return LogicStatus.CONTRADICTION;
            if (v == 0 && vpoints.size() > 2) continue;
            if (v == 0 && vpoints.size() == 1) return LogicStatus.CONTRADICTION;
            if (v > 0 && vpoints.size() > 1) continue;
            for (Point p : vpoints) {
                CellSet cs = thing.getCell(p.x,p.y);
                if (cs.isSolo()) continue;
                cs.set(v);
                result = LogicStatus.LOGICED;
            }
        }
        return result;
    }
}
