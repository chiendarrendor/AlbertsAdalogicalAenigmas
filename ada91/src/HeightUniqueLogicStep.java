import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HeightUniqueLogicStep implements LogicStep<Board> {
    List<Point> cells;
    public HeightUniqueLogicStep(List<Point> cells) { this.cells = cells; }

    @Override public LogicStatus apply(Board thing) {
        Map<Integer,List<CellState>> contains = new HashMap<>();
        Map<Integer,CellState> uniques = new HashMap<>();

        for (Point p : cells) {
            CellState cs = thing.getCell(p.x,p.y);
            if (cs.broken()) return LogicStatus.CONTRADICTION;
            if (cs.complete()) {
                if (uniques.containsKey(cs.unique())) return LogicStatus.CONTRADICTION;
                uniques.put(cs.unique(),cs);
            }
            for(int possible : cs.getSet()) {
                if (!contains.containsKey(possible)) contains.put(possible,new ArrayList<>());
                contains.get(possible).add(cs);
            }
        }

        // if we get here:  every CellState has at least one number in it
        // every CellState that has exactly one number is in uniques, and no two one-numbered CellStates
        // have the same number
        // every CellState is referenced in contains by the numbers it contains

        LogicStatus result = LogicStatus.STYMIED;

        for(int i = 1 ; i <= cells.size() ; ++i) {
            // if we've lost all instances of a number...fail
            if (!contains.containsKey(i)) return LogicStatus.CONTRADICTION;

            List<CellState> containers = contains.get(i);
            CellState unique = uniques.get(i);

            // so this is a terminal state for this number...only one cell has the number
            // and the cell with the number doesn't have any other numbers
            if (unique != null && containers.size() == 1) continue;

            // this is the unknown state:  no cell has just this number, and this number is referenced in multiple cells
            if (unique == null && containers.size() > 1) continue;

            // if we get here, either
            // 1) the number is unique in one cell, but exists in other cells
            // 2) the number exists in only one cell, but that cell contains other numbers
            if (unique == null) {
                // containers.size == 1
                containers.get(0).set(i);
                result = LogicStatus.LOGICED;
            } else {
                // unique is not null, but containers.size > 1
                for(CellState cs : containers) {
                    if (cs == unique) continue;
                    cs.remove(i);
                    result = LogicStatus.LOGICED;
                }
            }
        }

        return result;
    }
}
