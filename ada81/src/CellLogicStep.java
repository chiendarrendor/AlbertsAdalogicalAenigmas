import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CellLogicStep implements LogicStep<Board> {
    int x;
    int y;
    List<Point> adjacents = new ArrayList<Point>();
    public CellLogicStep(Board b, int x, int y) {
        for (Direction d: Direction.orthogonals()) {
            Point np = d.delta(x,y,1);
            if (!b.inBounds(np.x,np.y)) continue;
            adjacents.add(np);
        }
        this.x = x;
        this.y = y;
    }
    // them     me
    //      _   +   -   #   +-  +#  -#  +-#
    // _    X   X   X   X   X   X   X   X
    // +    X   X   s   s   P   P   s   P
    // -    X   s   X   s   N   s   N   N
    // #    X   s   s   s   s   s   s   s
    // +-   X           s
    // +#   X           s
    // -#   X           s
    // +-#  X           s





    @Override public LogicStatus apply(Board thing) {
        Cell curcell = thing.getCell(x,y);
        if (curcell.isBroken()) return LogicStatus.CONTRADICTION;
        if (curcell.isBlank()) return LogicStatus.STYMIED;

        LogicStatus result = LogicStatus.STYMIED;
        for(Point p : adjacents) {
            Cell pcell = thing.getCell(p.x,p.y);
            if (pcell.isBroken()) return LogicStatus.CONTRADICTION;
            if (pcell.isBlank()) continue;
            if (pcell.isPositive()) {
                if (curcell.isPositive()) return LogicStatus.CONTRADICTION;
                if (curcell.isNegative()) continue;
                if (curcell.canBePositive()) { curcell.clearPositive(); result = LogicStatus.LOGICED; }
            }
            if (pcell.isNegative()) {
                if (curcell.isNegative()) return LogicStatus.CONTRADICTION;
                if (curcell.isPositive()) continue;
                if (curcell.canBeNegative()) { curcell.clearNegative(); result = LogicStatus.LOGICED; }
            }
        }

        if (curcell.isBroken()) return LogicStatus.CONTRADICTION;
        return result;
    }

    @Override public String toString() { return "Cell Logic Step: " + x + "," + y;}
}
