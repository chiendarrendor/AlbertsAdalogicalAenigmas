import grid.logic.LogicStatus;
import grid.puzzlebits.Path.Path;

public class LoopLogicStep implements grid.logic.LogicStep<Board> {

    public LogicStatus apply(Board thing) {

        try {
            thing.getPaths().clean();
        } catch (BadMergeException bme) {

            return LogicStatus.CONTRADICTION;
        }

        int loopcount = 0;
        int pathcount = 0;
        for (Path p : thing.getPaths()) {
            ++pathcount;
            if (p.isClosed()) ++loopcount;
        }


        if (loopcount > 1) return LogicStatus.CONTRADICTION;
        if (loopcount == 1 && pathcount > 1) return LogicStatus.CONTRADICTION;
        return LogicStatus.STYMIED;
    }
}