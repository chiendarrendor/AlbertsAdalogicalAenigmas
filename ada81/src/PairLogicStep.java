import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;
import java.util.List;

public class PairLogicStep implements LogicStep<Board> {
    Point p1;
    Point p2;
    public PairLogicStep(List<Point> pair) { p1 = pair.get(0); p2 = pair.get(1); }


    // the CellLogicStep will take care of the +- dichotomy
    // what this clue has to take care of is the idea that
    // both ends of a pair must either be magnet-ends, or blanks.

    @Override public LogicStatus apply(Board thing) {
        Cell c1 = thing.getCell(p1.x,p1.y);
        Cell c2 = thing.getCell(p2.x,p2.y);

        LogicStatus result = LogicStatus.STYMIED;
        if (c1.isBroken() || c2.isBroken()) return LogicStatus.CONTRADICTION;
        if (c1.isBlank()) {
            // +# -# +-#
            if (!c2.canBeBlank()) return LogicStatus.CONTRADICTION; // + - +-
            if (c2.isBlank()) return LogicStatus.STYMIED; // #
            result = LogicStatus.LOGICED;
            c2.setBlank();
        }

        if (c2.isBlank()) {
            if (!c1.canBeBlank()) return LogicStatus.CONTRADICTION;
            if (c1.isBlank()) return LogicStatus.STYMIED;
            result = LogicStatus.LOGICED;
            c1.setBlank();
        }

        if (c1.isMagnetic()) {
            // +# -# +-#
            if (!c2.canBeMagnetic()) return LogicStatus.CONTRADICTION;
            if (c2.isMagnetic()) return LogicStatus.STYMIED;
            result = LogicStatus.LOGICED;
            c2.setMagnetic();
        }

        if (c2.isMagnetic()) {
            if (!c1.canBeMagnetic()) return LogicStatus.CONTRADICTION;
            if (c1.isMagnetic()) return LogicStatus.STYMIED;
            result = LogicStatus.LOGICED;
            c1.setMagnetic();
        }

        return result;
    }

    @Override public String toString() {
        return "PairLogicStep: " + p1 + " " + p2;
    }
}
