import grid.logic.LogicStatus;
import grid.puzzlebits.Direction;
import grid.puzzlebits.Path.Path;
import grid.solverrecipes.singleloopflatten.BadMergeException;

import java.awt.Point;

public class PathsLogicStep implements grid.logic.LogicStep<Board> {
    @Override public LogicStatus apply(Board thing) {
        try {
            thing.cleanPaths();
        } catch (BadMergeException bme) {
            return LogicStatus.CONTRADICTION;
        }

        LogicStatus result = LogicStatus.STYMIED;

        for (Path p : thing.getPaths()) {
            if (p.isClosed()) return LogicStatus.CONTRADICTION;
            // we no longer have to check straightness, because new Board.processCell checks that for us
            Board.CellProcessor cp1 = thing.processCell(p.endOne().x,p.endOne().y,CellType.UNKNOWN);
            Board.CellProcessor cp2 = thing.processCell(p.endTwo().x,p.endTwo().y,CellType.UNKNOWN);

            if (cp1.result == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
            if (cp2.result == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
            if (cp1.result == LogicStatus.LOGICED) result = LogicStatus.LOGICED;
            if (cp2.result == LogicStatus.LOGICED) result = LogicStatus.LOGICED;

            if (cp1.ct != CellType.TERMINAL) continue;
            if (cp2.ct != CellType.TERMINAL) continue;

            if (thing.getRegionId(p.endOne().x,p.endOne().y) == thing.getRegionId(p.endTwo().x,p.endTwo().y)) return LogicStatus.CONTRADICTION;
        }

        return result;
    }
}
