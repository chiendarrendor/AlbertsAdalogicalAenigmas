import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class RegionLogicStep implements LogicStep<Board> {
    List<Point> cells = new ArrayList<>();
    int targetnum = -1;

    public void addCell(int x, int y) { cells.add(new Point(x,y)); }
    public void setNumber(int number) { targetnum = number; }

    @Override public LogicStatus apply(Board thing) {
        if (targetnum == -1) return LogicStatus.STYMIED;

        List<Point> unknowns = new ArrayList<>();
        int horcount = 0;
        int vercount = 0;
        for (Point p : cells) {
            switch(thing.getCell(p.x,p.y)) {
                case HORIZONTAL: ++horcount; break;
                case VERTICAL: ++vercount; break;
                case UNKNOWN: unknowns.add(p); break;
            }
        }

        if (horcount > targetnum) return LogicStatus.CONTRADICTION;
        if (vercount > targetnum) return LogicStatus.CONTRADICTION;

        boolean canbehor = (horcount + unknowns.size()) >= targetnum;
        boolean canbever = (vercount + unknowns.size()) >= targetnum;

        if (!canbehor && !canbever) return LogicStatus.CONTRADICTION;
        if (canbehor && canbever) return LogicStatus.STYMIED;
        if (unknowns.size() == 0) return LogicStatus.STYMIED;

        CellState opcs = canbehor ? CellState.HORIZONTAL : CellState.VERTICAL;
        int knowncount = canbehor ? horcount : vercount;

        if (knowncount + unknowns.size() == targetnum) {
            for(Point p : unknowns) thing.setCell(p.x,p.y,opcs);
            return LogicStatus.LOGICED;
        }

        return LogicStatus.STYMIED;
    }


}
