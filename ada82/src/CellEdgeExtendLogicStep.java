import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;
import grid.solverrecipes.singleloopflatten.EdgeState;

import java.awt.Point;

public class CellEdgeExtendLogicStep extends ExtendLogicStep implements LogicStep<Board> {
    int x;
    int y;
    Direction d;
    // arguments are a cell and a direction...clue is on border between this cell and the one in the direction
    public CellEdgeExtendLogicStep(int x, int y, Direction d) { this.x = x; this.y = y; this.d = d; }


    @Override public LogicStatus apply(Board thing) {
        LogicStatus result = LogicStatus.STYMIED;
        switch (thing.getEdge(x,y,d)) {
            case UNKNOWN:
                thing.setEdge(x,y,d, EdgeState.PATH);
                result = LogicStatus.LOGICED;
                break;
            case PATH: break;
            case WALL: return LogicStatus.CONTRADICTION;
        }

        // if we get here, our cell (x,y) has a path to cell d.delta(x,y,1)
        Point op = d.delta(x,y,1);
        LogicStatus answer = extend(thing,x,y,op.x,op.y,d);
        if (answer == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
        if (answer == LogicStatus.LOGICED) result = LogicStatus.LOGICED;




        return result;
    }
}
