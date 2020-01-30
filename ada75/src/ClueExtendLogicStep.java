import grid.logic.LogicStatus;
import grid.logic.LogicStep;

public class ClueExtendLogicStep implements LogicStep<Board> {
    int x;
    int y;
    int size;
    public ClueExtendLogicStep(int x, int y,int size) { this.x = x; this.y = y; this.size = size; }

    // this logic step extends out the incomplete path as many spaces as it can go in the directions
    // available from its end to see if it is possible to get to a terminus.  any direction impossible
    // must be rejected, only one possible must be accepted.
    @Override public LogicStatus apply(Board thing) {








        return LogicStatus.STYMIED;
    }
}
