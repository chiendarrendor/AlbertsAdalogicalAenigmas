import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

public class NumericClueLogicStep extends CountingLogicStep {
    int number;
    public NumericClueLogicStep(Board b, int x, int y, int number) {
        for (Direction d : Direction.orthogonals()) {
            Point tp = d.delta(x,y,1);
            if (!b.inBounds(tp)) continue;
            addPoint(tp);
        }
        this.number = number;
    }

    @Override public LogicStatus apply(Board thing) {
        count(thing);

        if (shadedcount > number) return LogicStatus.CONTRADICTION;
        if (shadedcount + unknowns.size() < number) return LogicStatus.CONTRADICTION;
        if (unknowns.size() == 0) return LogicStatus.STYMIED;

        if (shadedcount == number) {
            unknowns.stream().forEach(p->thing.setCell(p.x,p.y,CellState.UNSHADED));
            return LogicStatus.LOGICED;
        }

        if (shadedcount + unknowns.size() == number) {
            unknowns.stream().forEach(p->thing.setCell(p.x,p.y,CellState.SHADED));
            return LogicStatus.LOGICED;
        }

        return LogicStatus.STYMIED;
    }
}
