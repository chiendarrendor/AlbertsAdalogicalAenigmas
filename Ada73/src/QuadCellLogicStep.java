import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.util.HashSet;
import java.util.Set;

public class QuadCellLogicStep implements LogicStep<Board> {
    int x;
    int y;
    public QuadCellLogicStep(int x, int y) { this.x = x; this.y = y; }


    @Override public LogicStatus apply(Board thing) {
        // A B
        // C D
        boolean aboverlaps = overlaps(thing,x,y,x+1,y);
        boolean cdoverlaps = overlaps(thing,x,y+1,x+1,y+1);
        boolean acoverlaps = overlaps(thing,x,y,x,y+1);
        boolean bdoverlaps = overlaps(thing,x+1,y,x+1,y+1);
        if (aboverlaps || cdoverlaps || acoverlaps || bdoverlaps) return LogicStatus.STYMIED;
        return LogicStatus.CONTRADICTION;
    }

    private boolean overlaps(Board thing, int x1, int y1, int x2, int y2) {
        Set<Tile> tset1 = thing.getCell(x1,y1).set;
        Set<Tile> tset2 = thing.getCell(x2,y2).set;
        Set<Tile> tempset = new HashSet<>();
        tempset.addAll(tset1);
        tempset.retainAll(tset2);
        return tempset.size() > 0;
    }
}
