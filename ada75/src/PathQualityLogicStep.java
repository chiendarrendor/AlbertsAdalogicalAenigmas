import grid.logic.LogicStatus;
import grid.puzzlebits.Direction;
import grid.puzzlebits.Turns;
import grid.solverrecipes.singleloopflatten.BadMergeException;
import grid.solverrecipes.singleloopflatten.EdgeState;

import java.awt.Point;
import java.util.List;

public class PathQualityLogicStep implements grid.logic.LogicStep<Board> {
    @Override public LogicStatus apply(Board thing) {
        //System.out.println("Starting PQLS");
        try {
            thing.cleanPaths();
        } catch(BadMergeException bme) {
            return LogicStatus.CONTRADICTION;
        }
        //System.out.println("PQLS Path cleaned");

        List<PathInfo> paths = thing.getPathInfo();
        LogicStatus result = LogicStatus.STYMIED;
        //System.out.println("PQLS " + paths.size() + " paths found");

        for (PathInfo pi : paths) {
            LogicStatus r = applyOne(thing,pi);
            if (r == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
            if (r == LogicStatus.LOGICED) result = LogicStatus.LOGICED;
        }

        return result;
    }

    private LogicStatus applyOne(Board thing, PathInfo pi) {
        if (pi.isLoop) return LogicStatus.CONTRADICTION;

        int minsize = 2;
        int maxsize = Integer.MAX_VALUE;
        int endcount = 0;

        if (pi.end1Terminated && pi.end2Terminated) {
            if (pi.end1Clue > 0 && pi.end2Clue > 0 && pi.end1Clue != pi.end2Clue) return LogicStatus.CONTRADICTION;
        }

        if (pi.end1Terminated) {
            ++endcount;
            if (pi.end1Clue > 0) minsize = maxsize  = pi.end1Clue;
        }

        if (pi.end2Terminated) {
            ++endcount;
            if (pi.end2Clue > 0) minsize = maxsize = pi.end2Clue;
        }

        if (endcount == 0) return LogicStatus.STYMIED;
        if (endcount == 2) {
            if (pi.size() < minsize || pi.size() > maxsize) return LogicStatus.CONTRADICTION;
            return LogicStatus.STYMIED;
        }

        // if we get here, we have only one end, and whatever we know about the length(either [1,MAX_INT), or
        // min = max = clue)
        if (pi.size() >= maxsize) return LogicStatus.CONTRADICTION;

        return LogicStatus.STYMIED;
    }
}
