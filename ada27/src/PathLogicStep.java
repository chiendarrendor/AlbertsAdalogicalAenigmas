import grid.logic.LogicStatus;
import grid.puzzlebits.newpath.PathContainer;

public class PathLogicStep implements grid.logic.LogicStep<Board> {
    @Override public LogicStatus apply(Board thing) {
        boolean cleanresult = thing.getPaths().clean();

        if (!cleanresult) return LogicStatus.CONTRADICTION;

        for (PathContainer.Path p : thing.getPaths().getPaths()) {
            if (p.isLoop()) return LogicStatus.CONTRADICTION;
        }

        return LogicStatus.STYMIED;
    }
}
