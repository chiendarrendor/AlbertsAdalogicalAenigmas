import grid.logic.LogicStatus;
import grid.logic.LogicStep;

public class PossessionLogicStep implements LogicStep<Board> {
    int x;
    int y;
    public PossessionLogicStep(int x, int y) { this.x = x; this.y = y; }

    @Override public LogicStatus apply(Board thing) {
        if (thing.getCell(x,y).getPathType() != PathType.INITIAL) return LogicStatus.STYMIED;
        JumpSet jset = thing.getJumpSet(x,y);
        int orig = jset.size();

        LogicStatus result = LogicStatus.STYMIED;

        jset.cleanBad(thing);
        if (jset.size() == 0) return LogicStatus.CONTRADICTION;
        if (jset.size() < orig) result = LogicStatus.LOGICED;

        if (jset.size() == 1) {
            jset.solo().place(thing);
            result = LogicStatus.LOGICED;
        }

        return result;
    }
}
