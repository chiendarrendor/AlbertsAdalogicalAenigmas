import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.awt.Point;

public class PotentialTreeLogicStep implements LogicStep<Board> {
    Point myp;

    public PotentialTreeLogicStep(int x, int y) { myp = new Point(x,y);  }

    @Override public LogicStatus apply(Board thing) {
        if (thing.getCell(myp.x,myp.y) != CellType.TREE) return LogicStatus.STYMIED;

        LogicStatus result = LogicStatus.STYMIED;

        for (Direction d : Direction.orthogonals()) {
            Point np = d.delta(myp,1);
            if (!thing.onBoard(np)) continue;
            switch(thing.getCell(np.x,np.y)) {
                case UNKNOWN:
                    thing.setCell(np.x,np.y,CellType.PATH);
                    result = LogicStatus.LOGICED;
                    break;
                case TREE:
                    return LogicStatus.CONTRADICTION;
                case PATH:
                    break;
            }
        }

        return result;
    }
}
