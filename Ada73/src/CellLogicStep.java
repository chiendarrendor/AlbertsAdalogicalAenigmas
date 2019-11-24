import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

public class CellLogicStep implements LogicStep<Board> {
    int x;
    int y;

    public CellLogicStep(int x, int y) { this.x = x; this.y = y;  }

    @Override public LogicStatus apply(Board thing) {
        if (thing.getCell(x,y).set.size() == 0) return LogicStatus.CONTRADICTION;
        if (thing.getCell(x,y).set.size() > 1) return LogicStatus.STYMIED;

        Tile mytile = thing.getCell(x,y).set.iterator().next();
        LogicStatus result = LogicStatus.STYMIED;

        int setstatus = thing.set(mytile);
        if (setstatus < 0) return LogicStatus.CONTRADICTION;
        if (setstatus > 0) result = LogicStatus.LOGICED;

        Set<Point> mycells = mytile.cells;
        int mysize = mycells.size();



        Set<Tile> doomed = new HashSet<>();
        for(Direction d : Direction.orthogonals()) {
            Point np = d.delta(x,y,1);
            if (!thing.onBoard(np)) continue;
            if (mycells.contains(np)) continue;
            for (Tile dt : thing.getCell(np.x,np.y).set) {
                if (dt.size() == mysize) {
                    doomed.add(dt);
                    result = LogicStatus.LOGICED;
                }

            }
        }

        for (Tile t : doomed) {
            thing.clear(t);
        }

        return result;
    }
}
