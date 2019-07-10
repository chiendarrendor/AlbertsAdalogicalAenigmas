import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;
import grid.solverrecipes.singleloopflatten.EdgeState;

import java.io.DataInput;

public class BlockLogicStep implements LogicStep<Board> {
    int x;
    int y;
    public BlockLogicStep(int x, int y) { this.x = x; this.y = y; }

    @Override public LogicStatus apply(Board thing) {
        LogicStatus result = LogicStatus.STYMIED;
        for (Direction d : Direction.orthogonals()) {
            switch(thing.getEdge(x,y,d)) {
                case UNKNOWN:
                    thing.setEdge(x,y,d, EdgeState.WALL);
                    result = LogicStatus.LOGICED;
                    break;
                case PATH:
                    return LogicStatus.CONTRADICTION;
                case WALL:
                    break;
            }

        }
        return result;
    }
}
