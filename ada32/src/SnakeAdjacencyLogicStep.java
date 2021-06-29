import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

public class SnakeAdjacencyLogicStep implements LogicStep<Board> {
    int x;
    int y;

    public SnakeAdjacencyLogicStep(int x, int y) { this.x = x; this.y = y; }

    @Override public LogicStatus apply(Board thing) {
        LogicStatus result = LogicStatus.STYMIED;
        NumCell ns = thing.getCell(x,y);
        if (ns.isBroken()) return LogicStatus.CONTRADICTION;
        if (!ns.isDone()) return LogicStatus.STYMIED;

        int myv = ns.getComplete();
        if (myv == 0) return LogicStatus.STYMIED;

        // if we get here, we are in a cell with a number and its number is myv

        // 'adjacent cell' means any non-empty, non-blocking,on-board cell orthogonally adjacent
        // 'num-set' = {my+1 if myv < 5, myv-1 if myv > 1)


        // calculate all 'adjacent cells'
        Set<Direction> actives = new HashSet<>();
        for(Direction d : Direction.orthogonals()) {
            Point p = d.delta(x,y,1);
            if (!thing.inBounds(p.x,p.y)) continue;
            NumCell anc = thing.getCell(p.x,p.y);
            if (anc.isBroken()) return LogicStatus.CONTRADICTION;
            if (anc.isDone() && anc.doesContain(0)) continue;
            actives.add(d);
        }

        Set<Integer> allowedAdjacents = new HashSet<>();
        if (myv < 5) {
            allowedAdjacents.add(myv+1);
        }
        if (myv > 1) {
            allowedAdjacents.add(myv-1);
        }

        // BROKEN:  does not take into account snake self-adjacency
        // for every adjacent cell, for every number not in allowedAdjacents
        // if cell has that number, remove it (and remember that we LOGICED)
        // also, it is possible that we make an adjacent cell empty this way...
        // we don't want it in active any more.

        // repurposing:  it _is_ true that a known number cannot be adjacent to itself!
        // better:  a number cannot be adjacent to self, +2/-2, or +4/-4
        Set<Integer> notvalidadjacent = new HashSet<>();
        notvalidadjacent.add(myv);
        if (myv+2 <= 5) notvalidadjacent.add(myv+2);
        if (myv+4 <= 5) notvalidadjacent.add(myv+4);
        if (myv-2 > 0 ) notvalidadjacent.add(myv-2);
        if (myv-4 > 0 ) notvalidadjacent.add(myv-4);

        Set<Direction> temp = new HashSet<>();
        for (Direction d : actives) {
            Point p = d.delta(x,y,1);
            NumCell dns = thing.getCell(p.x,p.y);
            for (int num = 1 ; num <= 5; ++num) {
                if (!dns.doesContain(num)) continue;
                if (!notvalidadjacent.contains(num)) continue;

                dns.remove(num);
                result = LogicStatus.LOGICED;
            }
            if (dns.isBroken()) return LogicStatus.CONTRADICTION;
            if (dns.isDone() && dns.doesContain(0)) continue;
            temp.add(d);
        }
        actives = temp;


        // for each num in num-set, cell must be adjacent t oexactly one cell with num
        // * if only one adjacent cell has that number, that cell must be set to that number
        // * if an adjacent cell is set to that number, all other adjacent cells must have that number removed
        for (int allowed : allowedAdjacents) {
            Direction must = null;
            Set<Direction> can = new HashSet<>();

            for (Direction d : actives) {
                Point p = d.delta(x,y,1);
                NumCell dns = thing.getCell(p.x,p.y);
                if (dns.isBroken()) return LogicStatus.CONTRADICTION;
                if (!dns.doesContain(allowed)) continue;
                if (dns.isDone()) {
                    // if we have more than one isDone with the same number adjacent, something's gone wrong
                    if (must != null) return LogicStatus.CONTRADICTION;
                    must = d;
                } else {
                    can.add(d);
                }
            }

            if (must != null) {
                for(Direction d : can) {
                    Point p = d.delta(x,y,1);
                    NumCell dns = thing.getCell(p.x,p.y);
                    dns.remove(allowed);
                    result = LogicStatus.LOGICED;
                }
            } else {
                if (can.size() == 0) return LogicStatus.CONTRADICTION;
                if (can.size() > 1) continue;

                Point p = can.iterator().next().delta(x,y,1);
                NumCell dns = thing.getCell(p.x,p.y);
                dns.set(allowed);
                result = LogicStatus.LOGICED;
            }
        }












        return result;
    }
}
