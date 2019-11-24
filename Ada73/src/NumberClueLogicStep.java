import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.util.HashSet;
import java.util.Set;

public class NumberClueLogicStep implements LogicStep<Board> {
    int x;
    int y;
    int number;
    public NumberClueLogicStep(int x, int y, int number) { this.x = x; this.y = y; this.number = number; }

    @Override public LogicStatus apply(Board thing) {
        TileSet ts = thing.getCell(x,y);
        LogicStatus result = LogicStatus.STYMIED;
        int numcount = 0;

        Set<Tile> tset = new HashSet<>();
        tset.addAll(ts.set);

        for (Tile t : tset) {
            int tsize = t.size();
            if (tsize == number) {
                ++numcount;
            } else {
                thing.clear(t);
                result = LogicStatus.LOGICED;
            }
        }
        if (numcount == 0) return LogicStatus.CONTRADICTION;
        return result;
    }
}
