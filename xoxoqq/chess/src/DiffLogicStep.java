import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.Set;
import java.util.stream.Collectors;

public class DiffLogicStep implements LogicStep<Board> {
    Point p1;
    Point p2;
    int delta;

    public String toString() { return "DiffLogicStep " + p1 + " " + p2 + " " + delta; }
    public DiffLogicStep(int x, int y, Direction diffDirection, int diffSize) {
        p1 = new Point(x,y);
        p2 = diffDirection.delta(p1,1);
        delta = diffSize;
    }

    @Override public LogicStatus apply(Board thing) {
        CellSet cs1 = thing.getCellSet(p1.x,p1.y);
        CellSet cs2 = thing.getCellSet(p2.x,p2.y);

        if (cs1.size() == 0) return LogicStatus.CONTRADICTION;
        if (cs2.size() == 0) return LogicStatus.CONTRADICTION;

        Set<Integer> del1 = cs1.stream().filter(x->!cs2.has(x-delta) && !cs2.has(x+delta)).collect(Collectors.toSet());
        Set<Integer> del2 = cs2.stream().filter(x->!cs1.has(x-delta) && !cs1.has(x+delta)).collect(Collectors.toSet());
        if (del1.size() == 0 && del2.size() == 0) return LogicStatus.STYMIED;
        del1.stream().forEach(x->cs1.isNot(x));
        del2.stream().forEach(x->cs2.isNot(x));

        return LogicStatus.LOGICED;
    }
}
