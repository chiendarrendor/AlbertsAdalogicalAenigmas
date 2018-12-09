import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import javax.naming.ldap.UnsolicitedNotification;
import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

public class NumberLogicStep implements LogicStep<Board> {
    Set<Point> adjacents = new HashSet<Point>();
    int size;
    public NumberLogicStep(Board b, int x, int y,int size) {
        this.size = size;
        Point cp = new Point(x,y);
        for (Direction d: Direction.orthogonals()) {
            Point dp = d.delta(cp,1);
            if (!b.onBoard(dp)) continue;
            if (b.getCell(dp.x,dp.y) == CellState.WHITE) continue;
            adjacents.add(dp);
        }
    }

    private Set<Point> expand(Board b,boolean includeUnknown) {
        // a cell in the queue we don't know anything about.
        Set<Point> queueSet = new HashSet<Point>();
        queueSet.addAll(adjacents);

        // a cell in result has been fully processed and is in the final set.
        Set<Point> result = new HashSet<>();

        // a cell in broken has been processed and found to be not in the final set.
        Set<Point> broken = new HashSet<>();

        while(queueSet.size() > 0) {
            Point curp = queueSet.iterator().next();
            queueSet.remove(curp);
            CellState curcs = b.getCell(curp.x,curp.y);

            // if a cell should not be in the set, break it
            if (curcs == CellState.WHITE) { broken.add(curp); continue; }
            if (curcs == CellState.UNKNOWN && !includeUnknown) { broken.add(curp); continue; }

            result.add(curp);

            for(Direction d : Direction.orthogonals()) {
                Point adjp = d.delta(curp,1);
                if (!b.onBoard(adjp)) continue;
                if (queueSet.contains(adjp)) continue;
                if (broken.contains(adjp)) continue;
                if (result.contains(adjp)) continue;
                queueSet.add(adjp);
            }
        }
        return result;
    }


    // calculate two point sets:
    // the set of all points starting from and including adjacents that are definitely black
    // the set of all points starting from and including adjacents that could be black
    @Override public LogicStatus apply(Board thing) {
        Set<Point> blacks = expand(thing,false);
        Set<Point> maybes = expand(thing,true);

        // very easy things:
        if (maybes.size() < size) return LogicStatus.CONTRADICTION;
        if (blacks.size() > size) return LogicStatus.CONTRADICTION;

        // this can only happen if some unknown is adjacent to the local black set.
        if (blacks.size() == size && maybes.size() > size) {
            boolean onefound = false;
            for (Point bp : blacks) {
                for (Direction d : Direction.orthogonals()) {
                    Point np = d.delta(bp,1);
                    if (!thing.onBoard(np)) continue;
                    if (thing.getCell(np.x,np.y) != CellState.UNKNOWN) continue;
                    thing.setCell(np.x,np.y,CellState.WHITE);
                    onefound = true;
                }
            }

            // special case:  the above mechanism finds all unknowns adjacent to the black set.
            // we have to add in any unknowns directly adjacent to the center, which isn't in the black set.
            for (Point adj : adjacents) {
                if (thing.getCell(adj.x,adj.y) != CellState.UNKNOWN) continue;
                thing.setCell(adj.x,adj.y,CellState.WHITE);
                onefound = true;
            }

            if (onefound) return LogicStatus.LOGICED;
            throw new RuntimeException("You missed a logic: blacks = size, maybes > size!");
        }

        // this should only happen if the amount of available space is what we need..but it's not all black yet.
        if (blacks.size() < size && maybes.size() == size) {
            boolean onefound = false;
            for (Point up : maybes) {
                if (thing.getCell(up.x,up.y) != CellState.UNKNOWN) continue;
                thing.setCell(up.x,up.y,CellState.BLACK);
                onefound = true;
            }
            if (onefound) return LogicStatus.LOGICED;
            throw new RuntimeException("You missed a logic: blacks < size,maybes =  size");
        }

        // overview:  if we need to get bigger, and there's only one way to do it, we have to make the black set bigger
        // that way.

        // set of valid points that we have not explored yet.
        Set<Point> probequeue = new HashSet<>();
        probequeue.addAll(adjacents);

        // set of all explored points that are black.
        Set<Point> blackset = new HashSet<>();
        // set of all explored points that are unknown
        Set<Point> edgeset = new HashSet<>();

        LogicStatus result = LogicStatus.STYMIED;

        while(true) {
            while(probequeue.size() > 0) {
                Point curp = probequeue.iterator().next();
                probequeue.remove(curp);
                CellState curcs = thing.getCell(curp.x, curp.y);
                if (curcs == CellState.WHITE) continue;
                if (curcs == CellState.UNKNOWN) {
                    edgeset.add(curp);
                    continue;
                }
                blackset.add(curp);
                for (Direction d : Direction.orthogonals()) {
                    Point adjp = d.delta(curp, 1);
                    if (!thing.onBoard(adjp)) continue;
                    if (blackset.contains(adjp)) continue;
                    if (edgeset.contains(adjp)) continue;
                    probequeue.add(adjp);
                }
            }
            // now when we get here, blackset contains all cells that are black back to
            // us, and edgeset contains the set of all edges that can expand us.
            // ES size/BS size:     0   1   >1
            //       bs < size:     X   E   ?
            //       bs = size:     -   C   C
            //       bs > size:     X   X   X

            if (blackset.size() > size) return LogicStatus.CONTRADICTION;

            if (blackset.size() == size) {
                for (Point p : edgeset) {
                    thing.setCell(p.x,p.y,CellState.WHITE);
                    result = LogicStatus.LOGICED;
                }
                return result;
            }

            // if we get here, blackset size < size
            if (edgeset.size() == 0) return LogicStatus.CONTRADICTION;
            if (edgeset.size() > 1) break;

            // if we get here, there's exactly one entry in edgeset.
            Point expander = edgeset.iterator().next();
            edgeset.clear();
            probequeue.add(expander); // this will then add the cell to blackset
            thing.setCell(expander.x,expander.y,CellState.BLACK);
            result = LogicStatus.LOGICED;
        }




        return result;
    }
}
