import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;

public class DifferentLogicStep implements LogicStep<Board> {
    Point p1;
    Point p2;

    public DifferentLogicStep(Point p1, Point p2) { this.p1 = p1; this.p2 = p2; }

    @Override public LogicStatus apply(Board thing) {
        CellSet cs1 = thing.getCellSet(p1.x,p1.y);
        CellSet cs2 = thing.getCellSet(p2.x,p2.y);

        if (cs1.size() == 0 || cs2.size() == 0) return LogicStatus.CONTRADICTION;

        if (cs1.isDone()) {
            int num1 = cs1.theNumber();

            if (cs2.isDone()) {
                int num2 = cs2.theNumber();
                if (num1 == num2) return LogicStatus.CONTRADICTION;
                return LogicStatus.STYMIED;
            } else {
                if (!cs2.has(num1)) return LogicStatus.STYMIED;
                cs2.isNot(num1);
                return LogicStatus.LOGICED;
            }
        } else {
            if (cs2.isDone()) {
                int num2 = cs2.theNumber();
                if (!cs1.has(num2)) return LogicStatus.STYMIED;
                cs1.isNot(num2);
                return LogicStatus.LOGICED;
            } else {
                return LogicStatus.STYMIED;
            }
        }
    }
}
