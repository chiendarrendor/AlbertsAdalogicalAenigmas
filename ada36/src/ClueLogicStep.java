import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;
import sun.security.krb5.internal.PAData;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class ClueLogicStep implements LogicStep<Board> {
    int x;
    int y;
    int size; // contains the number of other cells we should be able to see (no reason to count ourselves every time.
    public ClueLogicStep(int x, int y, int size) { this.x = x; this.y = y; this.size = size-1; }


    private class Chain {
        int min = 0;
        int max = 0;
        Direction d;
        List<Point> knowns = new ArrayList<>();
        List<Point> unknowns = new ArrayList<>();
        public Chain(Direction d, Board b) {
            this.d = d;
            boolean interrupted = false;
            for (int i = 1 ; ; ++i) {
                Point op = d.delta(x,y,i);
                if (!b.inBounds(op)) break;
                CellType ct = b.getCell(op.x,op.y);
                if (ct == CellType.OUTSIDE) break;
                ++max;
                switch(ct) {
                    case INSIDE:
                        if (!interrupted) { ++min; knowns.add(op); }
                        break;
                    case UNKNOWN:
                        interrupted = true;
                        unknowns.add(op);
                        break;
                }
            }
        }

        public int getMin() { return min; }
        public int getMax() { return max; }
    }



    @Override public LogicStatus apply(Board thing) {
        List<Chain> chains = new ArrayList<>();
        int min = 0;
        int max = 0;
        int ucount = 0;
        for (Direction d : Direction.orthogonals()) {
            Chain c = new Chain(d,thing);
            min += c.getMin();
            max += c.getMax();
            ucount += c.unknowns.size();
            if (max > 0) chains.add(c);
        }
        if (min > size) return LogicStatus.CONTRADICTION;
        if (max < size) return LogicStatus.CONTRADICTION;
        LogicStatus result = LogicStatus.STYMIED;

        if (size == max && ucount > 0) {
            for (Chain c : chains) {
                for(Point p : c.unknowns) {
                    thing.setCell(p.x,p.y,CellType.INSIDE);
                }
            }
            return LogicStatus.LOGICED;
        }

        if (size == min && ucount > 0) {
            for (Chain c : chains) {
                if (c.unknowns.size() == 0) continue;
                Point p = c.unknowns.get(0);
                thing.setCell(p.x,p.y,CellType.OUTSIDE);
            }
            return LogicStatus.LOGICED;
        }





        for (Chain c : chains) {
            // we will one by one remove each chain from the equation
            // if the other arms together don't have enough cells to make the result feasible, then
            // we will add cells one at a time.
            if (c.unknowns.size() == 0) continue; // no use in removing a constant-sized chain.
            int omax = max - c.getMax();
            if (omax + c.min < size) {
                result = LogicStatus.LOGICED;
                Point addp = c.unknowns.get(0);
                thing.setCell(addp.x,addp.y, CellType.INSIDE);
            }
        }







        return result;
    }
}
