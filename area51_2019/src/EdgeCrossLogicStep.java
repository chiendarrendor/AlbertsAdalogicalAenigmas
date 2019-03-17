import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.solverrecipes.genericloopyflatten.LineState;

import java.awt.Point;

public class EdgeCrossLogicStep implements LogicStep<Board> {
    Point p1;
    Point p2;
    String edgename;
    public EdgeCrossLogicStep(EdgeCross ec) { p1 = ec.p1; p2 = ec.p2; edgename = ec.edgename; }

    @Override public LogicStatus apply(Board thing) {
        CellState cs1 = CellState.OUTSIDE;
        CellState cs2 = CellState.OUTSIDE;
        if (thing.onBoard(p1)) cs1 = thing.getCellState(p1.x,p1.y);
        if (thing.onBoard(p2)) cs2 = thing.getCellState(p2.x,p2.y);
        LineState ls = thing.loopy.getEdge(edgename);
        if (cs1 == CellState.UNKNOWN && cs2 == CellState.UNKNOWN) return LogicStatus.STYMIED;

        if (cs1 != CellState.UNKNOWN && cs2 != CellState.UNKNOWN) {
            if (cs1 == cs2) {
                switch(ls) {
                    case NOTPATH: return LogicStatus.STYMIED;
                    case PATH: return LogicStatus.CONTRADICTION;
                    case UNKNOWN: thing.loopy.setEdge(edgename,LineState.NOTPATH); return LogicStatus.LOGICED;
                }
            } else {
                switch(ls) {
                    case PATH: return LogicStatus.STYMIED;
                    case NOTPATH: return LogicStatus.CONTRADICTION;
                    case UNKNOWN: thing.loopy.setEdge(edgename,LineState.PATH); return LogicStatus.LOGICED;
                }
            }
        }

        if (ls == LineState.UNKNOWN) return LogicStatus.STYMIED;
        Point unkp = cs1 == CellState.UNKNOWN ? p1 : p2;
        CellState unkcs = cs1 == CellState.UNKNOWN ? cs1 : cs2;
        Point knowp = cs1 == CellState.UNKNOWN ? p2 : p1;
        CellState knowcs = cs1 == CellState.UNKNOWN ? cs2 : cs1;
        CellState ocolor = knowcs == CellState.INSIDE ? CellState.OUTSIDE : CellState.INSIDE;

        thing.setCellState(unkp.x,unkp.y,ls == LineState.PATH ? ocolor : knowcs);
        return LogicStatus.LOGICED;
    }
}
