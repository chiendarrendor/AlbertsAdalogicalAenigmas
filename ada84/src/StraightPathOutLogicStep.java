import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StraightPathOutLogicStep implements LogicStep<Board> {
    TerminalLogicStep tls;
    int cx;
    int cy;
    int clue; // this is the number of non-self cells (we know we're part of path)
    public StraightPathOutLogicStep(int x, int y, int clue) {
        this.cx = x;
        this.cy = y;
        this.clue = clue-1;
        tls = new TerminalLogicStep(cx,cy);
    }

    private boolean canSet(Board thing,Direction d) {
        for (int i = 1 ; i <= clue ; ++i) {
            Point op = d.delta(cx,cy,i);
            if (!thing.inBounds(op.x,op.y)) return false;
            if (thing.getCell(op.x,op.y) == CellType.WALL) return false;
        }
        Point op = d.delta(cx,cy,clue+1);
        if (!thing.inBounds(op.x,op.y)) return true;
        if (thing.getCell(op.x,op.y) == CellType.PATH) return false;
        return true;
    }



    @Override public LogicStatus apply(Board thing) {
        LogicStatus result = LogicStatus.STYMIED;
        // we know we're a terminal...let's make sure we're legal as one before continuing.
        LogicStatus lstat = tls.apply(thing);
        if (lstat == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
        if (lstat == LogicStatus.LOGICED) result = LogicStatus.LOGICED;

        // if we get here, one of two things will be true: either exactly one edge will be a path,
        // or more than one edge will be unknown and none of them will be paths.
        // for any direction that are unknowns, then we should be able to place on the board exactly clue path cells and one wall cell
        //    if we can't do that, we can't go in that direction and the immediate cell in that direction should be a wall
        //    if we turn all unknowns into walls, contradiction.
        //    if we turn all but one into walls, then the last edge must be a path.
        //
        // if exactly one edge is a path, then we must set exactly clue path cells in that direction, plus one wall.

        Direction thed = null;
        Set<Direction> possd = new HashSet<>();
        for (Direction d : Direction.orthogonals()) {
            Point op = d.delta(cx,cy,1);
            if (!thing.inBounds(op)) continue;
            if (thing.getCell(op.x,op.y) == CellType.PATH) thed = d;
            if (thing.getCell(op.x,op.y) == CellType.UNKNOWN) possd.add(d);
        }

        if (possd.size() > 0) {
            for(Direction d : Direction.orthogonals()) {
                if (!possd.contains(d)) continue;
                if (canSet(thing,d)) continue;
                result = LogicStatus.LOGICED;
                Point op = d.delta(cx,cy,1);
                thing.setCell(op.x,op.y,CellType.WALL);
                possd.remove(d);
            }
            // we know that if there were any possibles, there won't be a known path (if there were,
            // the TerminalLogicPath would have turned all unknowns into walls already)
            // which means that all ways out are either walls already or unknowns.
            // we also know that there must be more than one possible (if there was only one possible, and no paths,
            // then all other edges are walls, and TerminalLogicPath would have made the remaining possible into a path)
            // if we made all unknowns into walls, we're stuck
            if (possd.size() == 0) return LogicStatus.CONTRADICTION;
            // if we only have one possible left, we made all other possibles into walls, and the remaining possible must be a path!
            if (possd.size() == 1) {
                Direction remainingd = possd.iterator().next();
                result = LogicStatus.LOGICED;
                Point op = remainingd.delta(cx,cy,1);
                thing.setCell(op.x,op.y,CellType.PATH);
                thed = remainingd;
            }
        }

        if (thed != null) {
            if (!canSet(thing,thed)) return LogicStatus.CONTRADICTION;
            for (int i = 1 ; i <= clue ; ++i) {
                Point op = thed.delta(cx,cy,i);
                if (thing.getCell(op.x,op.y) == CellType.UNKNOWN) {
                    result = LogicStatus.LOGICED;
                    thing.setCell(op.x,op.y,CellType.PATH);
                }
            }
            Point op = thed.delta(cx,cy,clue+1);
            if (thing.inBounds(op.x,op.y) && thing.getCell(op.x,op.y) == CellType.UNKNOWN) {
                result = LogicStatus.LOGICED;
                thing.setCell(op.x,op.y,CellType.WALL);
            }
        }






        return result;
    }
}
