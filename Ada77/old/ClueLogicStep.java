import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.List;

public class ClueLogicStep implements LogicStep<Board> {
    Point me;
    int size;
    Direction d;

    public ClueLogicStep(int x, int y, int clueSize, Direction clueDirection) { me = new Point(x,y); size = clueSize; d = clueDirection; }

    @Override public LogicStatus apply(Board thing) {
        if (thing.cells.getCell(me.x,me.y) == CellState.FINALBOX) return LogicStatus.STYMIED;
        // there are two parts to this
        // a) _can_ a crate end up here?
        // b) is this clue being violated?
        List<Point> crates = thing.canComeHere(me.x,me.y);
        boolean canBeHidden = crates.size() > 0;

        int minboxes = 0;
        int maxboxes = 0;

        for (int i = 1 ; ; ++i) {
            Point np = d.delta(me,i);
            if (!thing.onBoard(np)) break;
            CellState cs = thing.cells.getCell(np.x,np.y);
            switch(cs) {
                case HORPATH:
                case VERPATH:
                    break;
                case FINALBOX:
                    ++minboxes;
                    ++maxboxes;
                    break;
                case SOURCEBOX:
                case EMPTY:
                    if (thing.canComeHere(np.x,np.y).size() > 0) ++maxboxes;
                    break;
            }
        }

        boolean sizeinbounds = minboxes <= size && size <= maxboxes;

        if (!sizeinbounds && !canBeHidden) return LogicStatus.CONTRADICTION;
        return LogicStatus.STYMIED;
    }

    public String toString() {
        return "ClueLogicStep (" + me.x + "," + me.y + ") " + size + d;
    }
}
