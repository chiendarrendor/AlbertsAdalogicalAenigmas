import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class HillValleyLogicStep implements LogicStep<Board> {
    List<Point> cells;
    HillState[] hills;

    private void clearHills() { for (int i = 0 ; i < cells.size() ; ++i) hills[i] = HillState.UNKNOWN; }
    private boolean setHill(int idx, HillState state) {
        if (hills[idx] != HillState.UNKNOWN) return hills[idx] == state;
        for (int i = 0 ; i < cells.size() ; ++i) {
            if ((i%2) == (idx%2)) hills[i] = state;
            else hills[i] = (state == HillState.HILL ? HillState.VALLEY : HillState.HILL);
        }
        return true;
    }


    public HillValleyLogicStep(List<Point> cells) {
        this.cells = cells;
        hills = new HillState[cells.size()];
    }

    @Override public LogicStatus apply(Board thing) {
        LogicStatus result = LogicStatus.STYMIED;
        int min = thing.getMinNumber();
        int max = thing.getMaxNumber();
        clearHills();
        // first order of business, use all possible pieces of information to determine if we can get the hill-valley information
        // for this group of cells, and thence to validate consistency of existing data against hill-valley information
        // possible information:
        // 1) first is largest value                                    HILL
        // 2) last is largest value                                     HILL
        // 3) first is smallest value                                   VALLEY
        // 4) last is smallest value                                    VALLEY
        // 5) non first/last is largest or 2nd largest value            HILL
        // 6) non first/last is smallest or second smallest value       VALLEY
        // 7) for cells n and n+1, if largest(n) <= smallest(n+1)       VALLEY,HILL
        // 8) for cells n and n+1, if smallest(n) >= largest(n+1)       HILL,VALLEY
        for (int i = 0 ; i < cells.size() ; ++i) {
            Point curp = cells.get(i);
            CellData cd = thing.getCellData(curp.x,curp.y);
            if (!cd.isValid()) return LogicStatus.CONTRADICTION;
            boolean isFirst = i == 0;
            boolean isLast = i == cells.size()-1;

            if (cd.isComplete()) {
                if (isFirst) {
                    if (cd.getValue() == max) if (!setHill(i,HillState.HILL)) return LogicStatus.CONTRADICTION;
                    if (cd.getValue() == min) if (!setHill(i,HillState.VALLEY)) return LogicStatus.CONTRADICTION;
                } else if (isLast) {
                    if (cd.getValue() == max) if (!setHill(i,HillState.HILL)) return LogicStatus.CONTRADICTION;
                    if (cd.getValue() == min) if (!setHill(i,HillState.VALLEY)) return LogicStatus.CONTRADICTION;
                } else {
                    if (cd.getValue() == max || cd.getValue() == max-1) if (!setHill(i,HillState.HILL)) return LogicStatus.CONTRADICTION;
                    if (cd.getValue() == min || cd.getValue() == min+1) if (!setHill(i,HillState.VALLEY)) return LogicStatus.CONTRADICTION;
                }
            }
            if (isLast) break;
            Point nextp = cells.get(i+1);
            CellData ncd = thing.getCellData(nextp.x,nextp.y);
            if (!ncd.isValid()) return LogicStatus.CONTRADICTION;

            if (cd.largest() <= ncd.smallest()) if (!setHill(i,HillState.VALLEY)) return LogicStatus.CONTRADICTION;;
            if (cd.smallest() >= ncd.largest())  if (!setHill(i,HillState.HILL)) return LogicStatus.CONTRADICTION;
        }

        if (hills[0] == HillState.UNKNOWN) return LogicStatus.STYMIED;

        // if we get here, we have a consistent hill-valley relationship... we can clean up numbers in adjacent cells that would
        // inherent violate hill-valley status
        // out of n and n+1 determine which is HILL and which is VALLEY
        // remove from VALLEY all numbers >= HILL.largest()
        // remove from HILL all numbers <= VALLEY.smallest()


        for (int i = 0 ; i < cells.size() - 1; ++i) {
            Point hillP = cells.get(i);
            Point valP = cells.get(i+1);

            if (hills[i] == HillState.VALLEY) {
                hillP = cells.get(i+1);
                valP = cells.get(i);
            }

            CellData hillcd = thing.getCellData(hillP.x,hillP.y);
            CellData valcd = thing.getCellData(valP.x,valP.y);
            if (!hillcd.isValid() || !valcd.isValid()) return LogicStatus.CONTRADICTION;

            List<Integer> doomed = new ArrayList<>();
            for (int v : valcd.possibles()) {
                if (v >= hillcd.largest()) {
                    doomed.add(v);
                    result = LogicStatus.LOGICED;
                }
            }
            doomed.stream().forEach(v->valcd.clear(v));

            doomed.clear();
            for (int v : hillcd.possibles()) {
                if (v <= valcd.smallest()) {
                    doomed.add(v);
                    result = LogicStatus.LOGICED;
                }
            }
            doomed.stream().forEach(v->hillcd.clear(v));
        }






        return result;
    }
}
