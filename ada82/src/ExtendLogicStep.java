import grid.logic.LogicStatus;
import grid.puzzlebits.Direction;
import grid.solverrecipes.singleloopflatten.EdgeState;
import org.omg.CORBA.UNKNOWN;

import java.awt.Point;

public class ExtendLogicStep  {
    // given that a line extends into cell (x1,y1) in the opp(d) direction, and into (x2,y2) in the d direction
    // both lines should extend the same distance...add to the direction if possible, fail if not.


    LogicStatus extend(Board b, int x1, int y1, int x2, int y2, Direction d2) {
        LogicStatus result = LogicStatus.STYMIED;
        Direction d1 = d2.getOpp();

        while(true) {
            // can we leave the cells
            EdgeState es1 = b.getEdge(x1,y1,d1);
            EdgeState es2 = b.getEdge(x2,y2,d2);

            //     es1:     W   P   U
            // es2:     W   S   C   L
            //          P   C   +   ++
            //          U   L   ++  S
            if (es1 == EdgeState.WALL && es2 == EdgeState.PATH) {
                return LogicStatus.CONTRADICTION;
            } else if (es1 == EdgeState.PATH && es2 == EdgeState.WALL) {
                return LogicStatus.CONTRADICTION;
            }
            // if we get here, we're not dead yet.
            else if (es1 == EdgeState.UNKNOWN && es2 == EdgeState.UNKNOWN) {
                break;
            } else if (es1 == EdgeState.WALL && es2 == EdgeState.WALL) {
                break;
            }  else if (es1 == EdgeState.WALL && es2 == EdgeState.UNKNOWN) {
                b.setEdge(x2,y2,d2,EdgeState.WALL);
                result = LogicStatus.LOGICED;
                break;
            } else if (es1 == EdgeState.UNKNOWN && es2 == EdgeState.WALL) {
                b.setEdge(x1, y1, d1, EdgeState.WALL);
                result = LogicStatus.LOGICED;
                break;
            }
            // if we get here, we will have paths in both directions and will be looping again.
            else if (es1 == EdgeState.PATH && es2 == EdgeState.PATH) {
                // do nothing
            }
            else if (es1 == EdgeState.PATH && es2 == EdgeState.UNKNOWN) {
                b.setEdge(x2,y2,d2,EdgeState.PATH);
                result = LogicStatus.LOGICED;
            }
            else if (es1 == EdgeState.UNKNOWN && es2 == EdgeState.PATH) {
                b.setEdge(x1,y1,d1,EdgeState.PATH);
            }  else {
                throw new RuntimeException("How did we not get all the combinations?");
            }

            // if we get here, both cells are known to have outgoing paths in their direction
            Point p1 = d1.delta(x1,y1,1);
            x1 = p1.x;
            y1 = p1.y;
            Point p2 = d2.delta(x2,y2,1);
            x2 = p2.x;
            y2 = p2.y;
        }



        return result;
    }
}
