import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;
import sun.rmi.runtime.Log;

import java.awt.Point;

public class CellLogicStep implements LogicStep<Board> {
    Point me;

    public CellLogicStep(int x, int y) { me = new Point(x,y); }


    @Override public LogicStatus apply(Board thing) {
        CellSet cs = thing.getCellSet(me.x,me.y);

        if (cs.size() == 0) return LogicStatus.CONTRADICTION;
        if (cs.size() > 1) return LogicStatus.STYMIED;
        int curnum = cs.theNumber();

        LogicStatus result = LogicStatus.STYMIED;
        for(Direction d: Direction.orthogonals()) {
            for (int idx = 1 ; idx <= curnum ; ++idx) {
                Point np = d.delta(me,idx);
                if (!thing.onBoard(np.x,np.y)) break;
                if (thing.getRegion(np.x,np.y) == null) continue;
                CellSet npcs = thing.getCellSet(np.x,np.y);
                if (!npcs.has(curnum)) continue;
                npcs.isNot(curnum);
                result = LogicStatus.LOGICED;
            }
        }

        return result;

    }
}
