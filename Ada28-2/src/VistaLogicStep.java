import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

public class VistaLogicStep implements LogicStep<Board> {
    Point cenp;
    // the clue given includes the vista space itself, which is always a path.
    // it's just easier to count the other cells you can see from the vista.
    int sightcount;

    private class DirPath {
        int mincount = 0;
        int maxcount = 0;
    }

    public VistaLogicStep(int x, int y, int size) { cenp = new Point(x,y); sightcount = size - 1; }

    @Override public LogicStatus apply(Board thing) {
        int mincountsum = 0;
        int maxcountsum = 0;
        Map<Direction,DirPath> paths = new HashMap<>();

        for (Direction d : Direction.orthogonals()) {
            DirPath dp = new DirPath();
            paths.put(d,dp);
            boolean inmin = true;
            for (int i = 1 ; ; ++i) {
                Point curp = d.delta(cenp,i);
                if (!thing.onBoard(curp)) break;
                CellType ct = thing.getCell(curp.x,curp.y);
                if (ct == CellType.TREE) break;

                if (!inmin) {
                    ++dp.maxcount;
                } else if (ct == CellType.PATH) {
                    ++dp.mincount;
                    ++dp.maxcount;
                } else {
                    ++dp.maxcount;
                    inmin = false;
                }
            }
            mincountsum += dp.mincount;
            maxcountsum += dp.maxcount;
        }

        if (mincountsum > sightcount) return LogicStatus.CONTRADICTION;
        if (maxcountsum < sightcount) return LogicStatus.CONTRADICTION;
        if (mincountsum == maxcountsum) return LogicStatus.STYMIED;

        LogicStatus result = LogicStatus.STYMIED;



        return result;
    }
}
