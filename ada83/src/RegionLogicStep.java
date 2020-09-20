import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;
import grid.solverrecipes.singleloopflatten.EdgeState;
import sun.rmi.runtime.Log;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RegionLogicStep implements LogicStep<Board> {
    Collection<Point> cells;
    int size;

    public RegionLogicStep(Collection<Point> regionCells, int regionClue) { cells = regionCells; size = regionClue; }

    @Override public LogicStatus apply(Board thing) {
        int terminalcount = 0;
        int internalcount = 0;
        LogicStatus result = LogicStatus.STYMIED;
        List<Point> unknowns = new ArrayList<Point>();

        for (Point p : cells) {
            Board.CellProcessor cp = thing.processCell(p.x,p.y,CellType.UNKNOWN);
            if (cp.result == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
            if (cp.result == LogicStatus.LOGICED) result = LogicStatus.LOGICED;

            switch(cp.ct) {
                case UNKNOWN: unknowns.add(p); break;
                case TERMINAL: ++terminalcount; break;
                case INTERNAL: ++internalcount; break;
            }
        }
        if (terminalcount > size) return LogicStatus.CONTRADICTION;
        if (terminalcount + unknowns.size() < size) return LogicStatus.CONTRADICTION;
        if (unknowns.size() == 0) return LogicStatus.STYMIED;

        if (terminalcount == size) {
            for(Point p : unknowns) {
                Board.CellProcessor cp = thing.processCell(p.x,p.y,CellType.INTERNAL);
                switch(cp.result) {
                    case LOGICED: result = LogicStatus.LOGICED; break;
                    case STYMIED: break;
                    case CONTRADICTION: return LogicStatus.CONTRADICTION;
                }
            }
        }

        if (terminalcount + unknowns.size() == size) {
            for (Point p : unknowns) {
                Board.CellProcessor cp = thing.processCell(p.x,p.y,CellType.TERMINAL);
                switch(cp.result) {
                    case LOGICED: result = LogicStatus.LOGICED; break;
                    case STYMIED: break;
                    case CONTRADICTION: return LogicStatus.CONTRADICTION;
                }
            }
        }



        return result;
    }
}
