import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NoDuplicatesLogicStep implements LogicStep<Board> {
    int min;
    int max;
    List<Point> points;
    public NoDuplicatesLogicStep(int minNumber, int maxNumber, List<Point> points) {
        min = minNumber; max = maxNumber; this.points = points;
    }

    @Override public LogicStatus apply(Board thing) {
        LogicStatus result = LogicStatus.STYMIED;
        Map<Integer,List<Point>> indicesOfPossibles = new HashMap<>();
        Set<Integer> knowns = new HashSet<>();

        for(Point p : points) {
             CellData cd = thing.getCellData(p.x,p.y);
             switch(cd.possibles().size()) {
                 case 0: return LogicStatus.CONTRADICTION;
                 case 1:
                     // if more than one cell in this group is known to be the same value, that's wrong.
                     if (knowns.contains(cd.getValue())) return LogicStatus.CONTRADICTION;
                     knowns.add(cd.getValue());
                     break;
                 default:
                     for(int possible: cd.possibles()) {
                        if (!indicesOfPossibles.containsKey(possible)) indicesOfPossibles.put(possible,new ArrayList<Point>());
                        indicesOfPossibles.get(possible).add(p);
                     }
                     break;
             }
        }

        for (int known : knowns) {
            if (indicesOfPossibles.containsKey(known)) {
                for (Point toclear : indicesOfPossibles.get(known)) {
                    thing.getCellData(toclear.x,toclear.y).clear(known);
                    result = LogicStatus.LOGICED;
                }
            }
        }

        Map<Integer,List<Point>> contains = new HashMap<>();

        for (Point p : points) {
            CellData cd = thing.getCellData(p.x,p.y);
            if (!cd.isValid()) return LogicStatus.CONTRADICTION;
            for (int v : cd.possibles()) {
                if (!contains.containsKey(v)) contains.put(v,new ArrayList<Point>());
                contains.get(v).add(p);
            }
        }

        if(contains.size() < points.size()) return LogicStatus.CONTRADICTION;
        if(contains.size() > points.size()) return result;

        // if we get here, we have exactly as many numbers represented in possibles as are required to cover all cells
        // which means that every number must go into a cell, and if we have a cell that is the only representation of that number,
        // that's where it has to be.


        for (int idx : contains.keySet()) {
            if (contains.get(idx).size() > 1) continue;
            Point thep = contains.get(idx).get(0);
            CellData cd = thing.getCellData(thep.x,thep.y);
            if (cd.isComplete()) continue;
            cd.set(idx);
            result = LogicStatus.LOGICED;
        }

        return result;
    }
}
