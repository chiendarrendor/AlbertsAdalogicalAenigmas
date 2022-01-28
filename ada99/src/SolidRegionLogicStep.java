import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;
import java.util.Set;

public class SolidRegionLogicStep extends CountingLogicStep {
    public SolidRegionLogicStep(Set<Point> cells) {
        cells.stream().forEach(p->addPoint(p));
    }

    @Override public LogicStatus apply(Board thing) {
        count(thing);
        if (shadedcount > 0 && unshadedcount > 0) return LogicStatus.CONTRADICTION;
        if (unknowns.size() == 0) return LogicStatus.STYMIED;
        if (shadedcount > 0) {
            unknowns.stream().forEach(p->thing.setCell(p.x,p.y,CellState.SHADED));
            return LogicStatus.LOGICED;
        }
        if (unshadedcount > 0) {
            unknowns.stream().forEach(p->thing.setCell(p.x,p.y,CellState.UNSHADED));
            return LogicStatus.LOGICED;
        }


        return LogicStatus.STYMIED;
    }
}
