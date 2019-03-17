import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PodLogicStep implements LogicStep<Board> {
    Point cp;
    int ocellcount;
    // because we always know that we are on, it will be easier to just count the other cells besides us.
    public PodLogicStep(Point p, int podValue) { cp = p; ocellcount = podValue - 1; }

    private class DirectionInteger {
        private Map<Direction,Integer> drint = new HashMap<>();
        public void inc(Direction d) {
            if (!drint.containsKey(d)) drint.put(d,0);
            drint.put(d,drint.get(d) + 1);
        }
        public int get(Direction d) { return drint.containsKey(d) ? drint.get(d) : 0; }
        public Set<Direction> dirs() { return drint.keySet(); }
        public int sum() { return sum(null); }
        public int sum(Direction fd) {
            int result=0;
            for (Direction d : drint.keySet()) {
                if (d == fd) continue;
                result += drint.get(d);
            }
            return result;
        }
    }

    // this method will add count INSIDE cells to the board in the direction d
    //
    private LogicStatus grow(Board thing, Direction d, int count) {
       LogicStatus result = LogicStatus.STYMIED;

       for (int i = 1 ; i <= count ; ++i) {
           Point tp = d.delta(cp,i);
           if (thing.getCellState(tp.x,tp.y) == CellState.UNKNOWN) {
               thing.setCellState(tp.x,tp.y,CellState.INSIDE);
               result = LogicStatus.LOGICED;
           }
       }
       return result;
    }



    @Override public LogicStatus apply(Board thing) {
        if (thing.getCellState(cp.x,cp.y) != CellState.INSIDE) return LogicStatus.CONTRADICTION;

        DirectionInteger mins = new DirectionInteger();
        DirectionInteger maxes = new DirectionInteger();
        for (Direction d : Direction.orthogonals()) {
            int index = 1;
            boolean minstop = false;
            while(true) {
                Point curp = d.delta(cp,index);
                if (!thing.onBoard(curp)) break;
                CellState cs = thing.getCellState(curp.x,curp.y);
                if (cs == CellState.OUTSIDE) break;
                maxes.inc(d);
                if (cs == CellState.UNKNOWN) minstop = true;
                if (!minstop) mins.inc(d);

                ++index;
            }
        }
        if (mins.sum() > ocellcount) return LogicStatus.CONTRADICTION;
        if (maxes.sum() < ocellcount) return LogicStatus.CONTRADICTION;

        LogicStatus result = LogicStatus.STYMIED;
        for (Direction d : maxes.dirs()) {
            int omaxsum = maxes.sum(d);
            if (omaxsum < ocellcount) {
                LogicStatus gresult = grow(thing,d,ocellcount-omaxsum);
                if (gresult == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
                if (gresult == LogicStatus.LOGICED) result = LogicStatus.LOGICED;
            }
        }

        return result;
    }


}
