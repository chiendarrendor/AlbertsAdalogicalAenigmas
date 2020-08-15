import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;
import grid.solverrecipes.singleloopflatten.EdgeState;

import java.awt.Point;
import java.util.List;

public class CellCenterExtendLogicStep extends ExtendLogicStep implements LogicStep<Board> {
    int x;
    int y;
    public CellCenterExtendLogicStep(int x, int y) { this.x = x; this.y = y; }

    @Override public LogicStatus apply(Board thing) {
        // first order of business...cell must have a path going straight through:
        LogicStatus result = LogicStatus.STYMIED;

        for (Direction d: Direction.orthogonals()) {
            if (thing.getEdge(x,y,d) == EdgeState.UNKNOWN) continue;
            if (thing.getEdge(x,y,d.getOpp()) == EdgeState.UNKNOWN) {
                thing.setEdge(x,y,d.getOpp(),thing.getEdge(x,y,d));
                result = LogicStatus.LOGICED;
            } if (thing.getEdge(x,y,d) != thing.getEdge(x,y,d.getOpp())) {
                return LogicStatus.CONTRADICTION;
            }
        }

        Direction godir = null;
        for(Direction d : Direction.orthogonals()) {
            if (thing.getEdge(x,y,d) == EdgeState.PATH && thing.getEdge(x,y,d.getOpp()) == EdgeState.PATH) {
                godir = d;
                break;
            }
        }
        if (godir == null) return result;

        Point p1 = godir.getOpp().delta(x,y,1);
        Point p2 = godir.delta(x,y,1);

        LogicStatus answer = extend(thing,p1.x,p1.y,p2.x,p2.y,godir);
        if (answer == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
        if (answer == LogicStatus.LOGICED) result = LogicStatus.LOGICED;
        
        return result;
    }
}
