import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class LineLogicStep implements LogicStep<Board> {
    int bid;
    int clue;
    List<Point> points;

    public LineLogicStep(int bid, int clue, List<Point> points) { this.bid = bid; this.clue = clue; this.points = points; }


    // returns the positive distance between two points on an orthogonal line.
    private static int lineDist(Point p1, Point p2) {
        int dx = p1.x - p2.x;
        int dy = p1.y - p2.y;
        if (dx < 0) dx = -dx;
        if (dy < 0) dy = -dy;
        if (dx != 0 && dy != 0) throw new RuntimeException("Points not on orthogonal line!");
        if (dx == 0 && dy == 0) throw new RuntimeException("Points identical!");
        return dx + dy;
    }


    @Override public LogicStatus apply(Board thing) {
        LogicStatus result = LogicStatus.STYMIED;
        SubBoard sb = thing.getSubBoard(bid);
        List<Point> unknowns = new ArrayList<>();
        List<Point> blacks = new ArrayList<>();
        int whitecount = 0;

        for (Point p : points) {
            switch(sb.getCell(p.x,p.y)) {
                case UNKNOWN: unknowns.add(p); break;
                case BLACK: blacks.add(p); break;
                case WHITE: ++whitecount; break;
            }
        }

        if (blacks.size() > 2) return LogicStatus.CONTRADICTION;
        if (blacks.size() + unknowns.size() < 2) return LogicStatus.CONTRADICTION;

        if (unknowns.size() > 0) {
            if (blacks.size() == 2) {
                unknowns.stream().forEach(p->sb.setCell(p.x,p.y,CellState.WHITE));
                unknowns.clear();
                result = LogicStatus.LOGICED;
            } else if (blacks.size() + unknowns.size() == 2) {
                unknowns.stream().forEach(p->sb.setCell(p.x,p.y,CellState.BLACK));
                blacks.addAll(unknowns);
                unknowns.clear();
                result = LogicStatus.LOGICED;
            }
        }
        // if we get here, we know:
        // blacks size is 0, 1, or 2, and has been correctly adjusted if we LOGICED
        // however,they may not be in order, so we should reprocess
        // in any case, we only have to do that part if we have an actual clue.
        if (clue < 0) return result;

        // given, for example, a clue of 4:
        // index  456789
        // color  BWWWWB
        // so, index 4(idx) demands that idx + clue + 1 = 9 must be black
        // and index 9(idx) demands that idx - clue - 1 = 4 must be black

        if (blacks.size() == 2) {
            if (lineDist(blacks.get(0),blacks.get(1)) != clue + 1) {
                return LogicStatus.CONTRADICTION;
            }
            return result;
        }

        if (blacks.size() == 0) return result;
        // if we get here, we have a black...there are at most two possible places where the other
        // one can be, and all others must be white.
        // 1) find the index in points where the black cell is.
        int bkidx;
        for (bkidx = 0 ; bkidx < points.size() ; ++bkidx) {
            Point p = points.get(bkidx);
            if (sb.getCell(p.x,p.y) == CellState.BLACK) break;
        }

        // determine if the location earlier in the list is even on the map, and if it is, is it not white?
        int lowidx = bkidx - (clue + 1);
        if (lowidx < 0) lowidx = -1;
        else if (sb.getCell(points.get(lowidx).x,points.get(lowidx).y) == CellState.WHITE) lowidx = -1;

        // determine if the location later in the list is even on the map, and if it is, is it not white?
        int highidx = bkidx + (clue + 1);
        if (highidx >= points.size()) highidx = -1;
        else if (sb.getCell(points.get(highidx).x,points.get(highidx).y) == CellState.WHITE) highidx = -1;

        // if neither places is valid, we have a problem.
        if (lowidx == -1 && highidx == -1) return LogicStatus.CONTRADICTION;

        // if one place is invalid, the other one must be our new black.
        int foundblackidx = -1;
        if (lowidx == -1)  foundblackidx = highidx;
        if (highidx == -1) foundblackidx = lowidx;

        // for each cell in the list, if we found the other black, mark it black,
        // otherwise, mark every cell not possible as a black as white.
        for (int pidx = 0 ; pidx < points.size() ; ++pidx) {
            Point curp = points.get(pidx);
            if (pidx == bkidx) continue;
            if (pidx == foundblackidx) {
                sb.setCell(curp.x,curp.y,CellState.BLACK);
                result = LogicStatus.LOGICED;
            }
            if (pidx == lowidx) continue;
            if (pidx == highidx) continue;
            if (sb.getCell(curp.x,curp.y) != CellState.UNKNOWN) continue;
            sb.setCell(curp.x,curp.y,CellState.WHITE);
            result = LogicStatus.LOGICED;
        }


        return result;
    }
}
