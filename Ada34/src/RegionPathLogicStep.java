import grid.logic.ContainerRuntimeException;
import grid.logic.LogicStatus;
import grid.puzzlebits.Path.Path;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

public class RegionPathLogicStep implements grid.logic.LogicStep<Board> {
    @Override public LogicStatus apply(Board thing) {
        LogicStatus result = LogicStatus.STYMIED;
        thing.cleanPaths();

        for (Path p : thing.getPaths()) {
            LogicStatus ls = applyOnePath(thing,p);
            if (ls == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
            if (ls == LogicStatus.LOGICED) result = LogicStatus.LOGICED;
        }
        return result;
    }

    public LogicStatus applyOnePath(Board thing, Path thepath) {
        int terminalcount = 0;
        int prebendcount = 0;
        int bendcount = 0;
        int postbendcount = 0;
        int sizeclue = 0;
        Set<Point> unknowns = new HashSet<>();

        LogicStatus result = LogicStatus.STYMIED;

        for (Point p : thepath) {
            if (thing.getCellType(p.x,p.y) == CellType.TERMINAL) ++terminalcount;
            if (thing.getCellType(p.x,p.y) == CellType.BEND) {
                ++bendcount;
            } else {
                if (bendcount == 0) ++prebendcount;
                else ++postbendcount;
            }
            if (thing.getCellType(p.x,p.y) == CellType.UNKNOWN) unknowns.add(p);
            if (thing.isNumberClue(p.x,p.y)) sizeclue = thing.getNumberClue(p.x,p.y);
        }
        if (bendcount > 1) return LogicStatus.CONTRADICTION;
        if (terminalcount > 2) return LogicStatus.CONTRADICTION;
        if (terminalcount == 2 && bendcount < 1) return LogicStatus.CONTRADICTION;

        if (bendcount == 1) unknowns.stream().forEach(p->thing.setCellType(p.x,p.y,CellType.NOTBEND));

        if (sizeclue == 0) return result;
        if (thepath.size() > sizeclue) return LogicStatus.CONTRADICTION;
        if (terminalcount == 2 && thepath.size() < sizeclue) return LogicStatus.CONTRADICTION;

        if (thepath.size() == sizeclue) {
            Point e1 = thepath.endOne();
            Point e2 = thepath.endTwo();

            CellType ct1 = thing.getCellType(e1.x,e1.y);
            CellType ct2 = thing.getCellType(e2.x,e2.y);

            if (ct1 == CellType.BEND) return LogicStatus.CONTRADICTION;
            if (ct2 == CellType.BEND) return LogicStatus.CONTRADICTION;

            if (ct1 != CellType.TERMINAL) {
                thing.setCellType(e1.x,e1.y,CellType.TERMINAL);
                result = LogicStatus.LOGICED;
            }

            if (ct2 != CellType.TERMINAL) {
                thing.setCellType(e2.x,e2.y,CellType.TERMINAL);
                result = LogicStatus.LOGICED;
            }
        }



        return result;
    }
}
