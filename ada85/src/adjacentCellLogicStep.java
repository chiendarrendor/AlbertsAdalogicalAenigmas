import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class adjacentCellLogicStep implements LogicStep<Board> {
    int x;
    int y;
    List<Point> adjacents = new ArrayList<>();

    public adjacentCellLogicStep(Board b,int x, int y) {
        this.x = x;
        this.y = y;
        for (Direction d : Direction.orthogonals()) {
            Point op = d.delta(x,y,1);
            if (b.inBounds(op.x,op.y)) adjacents.add(op);
        }
    }

    @Override public LogicStatus apply(Board thing) {
        CellContents cc = thing.getCell(x,y);
        if (cc.size() == 0) return LogicStatus.CONTRADICTION;
        if (cc.size() > 1) return LogicStatus.STYMIED;
        int mynumber = cc.getPossibles().iterator().next();

        LogicStatus result = LogicStatus.STYMIED;
        for(Point op : adjacents) {
            CellContents occ = thing.getCell(op.x,op.y);
            if (occ.size() == 0) return LogicStatus.CONTRADICTION;
            if (!occ.contains(mynumber)) continue;
            if (occ.size() == 1) return LogicStatus.CONTRADICTION;
            occ.clear(mynumber);
            result = LogicStatus.LOGICED;
        }

        return result;
    }
}
