import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegionLogicStep implements LogicStep<Board> {
    List<Point> cells;
    public RegionLogicStep(List<Point> cells) { this.cells = cells; }

    // checking two things:
    // 1) if any cell is known, all other cells must not have that number
    // 2) for every possible number in region, at least one cell must have that number
    @Override public LogicStatus apply(Board thing) {
        LogicStatus result = LogicStatus.STYMIED;

        Map<Integer,Point> knowns = new HashMap<>();
        for (Point p : cells) {
            CellContents cc = thing.getCell(p.x,p.y);
            if (cc.size() == 0) return LogicStatus.CONTRADICTION;
            if (cc.size() > 1) continue;
            int v = cc.getPossibles().iterator().next();
            if (knowns.containsKey(v)) return LogicStatus.CONTRADICTION;
            knowns.put(v,p);
        }
        // so if we get here,
        // 1) no cells are currently empty
        // 2) no known cells are duplicates of each other
        // 3) every known cell is listed in knowns
        for (int knownid : knowns.keySet()) {
            for (Point p : cells) {
                if (p.equals(knowns.get(knownid))) continue;
                CellContents cc = thing.getCell(p.x,p.y);
                if (!cc.contains(knownid)) continue;
                if (cc.size() == 1) return LogicStatus.CONTRADICTION;
                cc.clear(knownid);
                result = LogicStatus.LOGICED;
            }
        }

        // so, if we get here
        // 1) no cells are empty
        // 2) no known cells are duplicates
        // 3) every known cell is not possible anywhere else.

        // create a list of all cells that are possible for a given number
        Map<Integer,List<Point>> possibles = new HashMap<>();
        for(Point p : cells) {
            CellContents cc = thing.getCell(p.x,p.y);
            for(int poss : cc.getPossibles()) {
                if (!possibles.containsKey(poss)) possibles.put(poss,new ArrayList<>());
                possibles.get(poss).add(p);
            }
        }

        for (int i = 1 ; i <= cells.size() ; ++i) {
            if (!possibles.containsKey(i)) return LogicStatus.CONTRADICTION;
            if (possibles.get(i).size() > 1) continue;
            Point theOnly = possibles.get(i).get(0);
            CellContents cc = thing.getCell(theOnly.x,theOnly.y);
            if (cc.size() == 1) continue;
            cc.set(i);
            result = LogicStatus.LOGICED;
        }



        return result;
    }
}
