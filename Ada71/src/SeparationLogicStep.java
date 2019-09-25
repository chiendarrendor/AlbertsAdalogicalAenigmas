import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class SeparationLogicStep implements LogicStep<Board> {
    private int intersum;
    List<Point> cells;
    public SeparationLogicStep(int intersum, List<Point> cells) { this.intersum = intersum; this.cells = cells; }

    @Override public LogicStatus apply(Board thing) {
        LogicStatus result = LogicStatus.STYMIED;
        List<Point> blacks = new ArrayList<>();
        for (Point p : cells) {
            CellSet cs = thing.getCell(p.x,p.y);
            if (!cs.isSolo()) continue;
            if (cs.getSolo() != CellSet.BLACK) continue;
            blacks.add(p);
        }
        if (blacks.size() == 0) return LogicStatus.STYMIED;
        if (blacks.size() > 2) return LogicStatus.CONTRADICTION;
        SeparationCalculator sepcalc = new SeparationCalculator(blacks.get(0),thing,cells,intersum);
        if (blacks.size() == 1) {
            for (Point p : cells) {
                if (p == blacks.get(0)) continue;
                if (!thing.getCell(p.x,p.y).has(CellSet.BLACK)) continue;
                sepcalc.addOtherEnd(p);
            }
        } else {
            sepcalc.addOtherEnd(blacks.get(1));
        }

        if (sepcalc.getTerminalCount() == 0) return LogicStatus.CONTRADICTION;
        if (sepcalc.getTerminalCount() > 1) return LogicStatus.STYMIED;

        Terminal t = sepcalc.getSoloTerminal();

        Point p = t.getTerminalPoint();
        CellSet cs = thing.getCell(p.x,p.y);
        if (!cs.isSolo()) {
            cs.set(CellSet.BLACK);
            result = LogicStatus.LOGICED;
        }

        for (Point tp : t.getInterPoints()) {
            cs = thing.getCell(tp.x,tp.y);
            if (cs.has(CellSet.BLACK)) {
                cs.remove(CellSet.BLACK);
                result = LogicStatus.LOGICED;
            }
        }

        return result;
    }
}
