import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.awt.Point;

public class AdjacentShapeLogicStep implements LogicStep<Board> {
    int x;
    int y;
    public AdjacentShapeLogicStep(int x, int y) { this.x = x; this.y = y; }

    @Override public LogicStatus apply(Board thing) {
        Cell cell = thing.getCell(x,y);
        if (cell.isEmpty()) return LogicStatus.CONTRADICTION;
        if (!cell.isDone()) return LogicStatus.STYMIED;
        if (cell.isBlank()) return LogicStatus.STYMIED;

        // if we get here, we know that this cell has a particular non-blank shape.
        // all other adjacent cells (including diagonally) must _not_ have this shape.
        CellShape cs = cell.getShape();

        LogicStatus result = LogicStatus.STYMIED;
        for (Direction d : Direction.values()) {
            Point ap = d.delta(x,y,1);
            if (!thing.onBoard(ap)) continue;
            Cell apcell = thing.getCell(ap.x,ap.y);
            if (!apcell.hasPossible(cs)) continue;
            apcell.remove(cs);
            result = LogicStatus.LOGICED;
        }



        return result;
    }
}
