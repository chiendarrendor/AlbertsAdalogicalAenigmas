import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class QuadCellLogicStep implements LogicStep<Board> {
    List<Point> cells = new ArrayList<>();
    public QuadCellLogicStep(int x, int y) {
        cells.add(new Point(x,y)); cells.add(new Point(x+1,y));
        cells.add(new Point(x,y+1)); cells.add(new Point(x+1,y+1));
    }

    @Override public LogicStatus apply(Board thing) {
        List<Point> unknowns = new ArrayList<>();
        int blackcount = 0;
        int whitecount = 0;

        for (Point p : cells) {
            switch(thing.getCellType(p.x,p.y)) {
                case UNKNOWN: unknowns.add(p); break;
                case BLACK: ++blackcount; break;
                case WHITE: ++whitecount; break;
            }
        }

        if (blackcount == 4) return LogicStatus.CONTRADICTION;
        if (unknowns.size() == 0) return LogicStatus.STYMIED;
        if (blackcount == 3) {
            Point unk = unknowns.get(0);
            thing.setCellType(unk.x,unk.y,CellType.WHITE);
            return LogicStatus.LOGICED;
        }

        return LogicStatus.STYMIED;
    }
}
