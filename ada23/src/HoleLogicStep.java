import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.util.List;
import java.awt.Point;

public class HoleLogicStep implements LogicStep<Board> {
    Point p;
    public HoleLogicStep(Point p) { this.p = p; }


    @Override
    public LogicStatus apply(Board thing) {
        Hole h = thing.holes.get(p);
        if (h.isSet()) return LogicStatus.STYMIED;

        List<ShotName> origSet = h.getShotNames();
        if (origSet.size() == 0) return LogicStatus.CONTRADICTION;

        h.validate();
        List<ShotName> newSet = h.getShotNames();
        if (newSet.size() == 0) return LogicStatus.CONTRADICTION;

        LogicStatus result = LogicStatus.STYMIED;
        if (newSet.size() == 1 ) {
            result = LogicStatus.LOGICED;
            h.set(newSet.get(0));
        } else if (newSet.size() < origSet.size()) {
            result = LogicStatus.LOGICED;
        }

        return result;
    }
}
