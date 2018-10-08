package grid.solverrecipes.singleloopflatten;

import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Path.Path;

public class SinglePathLogicStep<T extends SingleLoopBoard<T>> implements LogicStep<T> {
    @Override public LogicStatus apply(T thing) {

        try {
            thing.cleanPaths();
        } catch (BadMergeException bme) {
            return LogicStatus.CONTRADICTION;
        }

        int pathcount = 0;
        int loopcount = 0;

        for (Path p : thing.getPaths()) {
            ++pathcount;
            if (p.isClosed()) ++loopcount;
        }

        if (loopcount > 1) return LogicStatus.CONTRADICTION;
        if (pathcount > 1 && loopcount == 1) return LogicStatus.CONTRADICTION;

        return LogicStatus.STYMIED;
    }
}
