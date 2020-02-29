import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;
import grid.solverrecipes.singleloopflatten.EdgeState;
import sun.rmi.runtime.Log;

import java.awt.Point;

public class IceLogicStep implements LogicStep<Board> {
    int x;
    int y;
    public IceLogicStep(Point p) { x = p.x ; y = p.y; }

    @Override public LogicStatus apply(Board thing) {
        LogicStatus result = LogicStatus.STYMIED;

        for (Direction d : Direction.orthogonals()) {
            EdgeState es = thing.getEdge(x,y,d);
            EdgeState opp = thing.getEdge(x,y,d.getOpp());
            switch(es) {
                case UNKNOWN:
                    switch(opp) {
                        case UNKNOWN: break;
                        case PATH:
                            thing.setPath(x,y,d);
                            result = LogicStatus.LOGICED;
                            break;
                        case WALL:
                            thing.setWall(x,y,d);
                            result = LogicStatus.LOGICED;
                            break;
                    }
                    break;
                case PATH:
                    if (opp == EdgeState.WALL) return LogicStatus.CONTRADICTION;
                    break;
                case WALL:
                    if (opp == EdgeState.PATH) return LogicStatus.CONTRADICTION;
                    break;
            }
        }
        return result;
    }
}
