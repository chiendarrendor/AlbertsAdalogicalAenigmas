import grid.logic.LogicStatus;
import grid.puzzlebits.Path.Path;
import grid.solverrecipes.singleloopflatten.BadMergeException;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class PathsLogicStep implements grid.logic.LogicStep<Board> {

    @Override public LogicStatus apply(Board thing) {
        try {
            thing.cleanPaths();
        } catch (BadMergeException bme) {
            return LogicStatus.CONTRADICTION;
        }


        List<Path> completes = new ArrayList<>();
        List<Path> loved = new ArrayList<>();

        for (Path p : thing.getPaths()) {
            if (p.isClosed()) return LogicStatus.CONTRADICTION;
            Point p1 = p.endOne();
            Point p2 = p.endTwo();

            if (thing.isLover(p1.x,p1.y) && thing.isLover(p2.x,p2.y)) return LogicStatus.CONTRADICTION;
            if (thing.isMate(p1.x,p1.y) && thing.isMate(p2.x,p2.y)) return LogicStatus.CONTRADICTION;
            if (thing.isLover(p1.x,p1.y) && thing.isMate(p2.x,p2.y)) { completes.add(p); continue; }
            if (thing.isMate(p1.x,p1.y) && thing.isLover(p2.x,p2.y)) { p.reverse(); completes.add(p); continue; }
            if (thing.isLover(p1.x,p1.y) ) { loved.add(p); continue; }
            if (thing.isLover(p2.x,p2.y)) { p.reverse(); loved.add(p); continue; }
        }

        for (Path p : completes) {
            if (p.size() != thing.getLoverNumber(p.endOne().x,p.endOne().y)) return LogicStatus.CONTRADICTION;
        }

        for (Path p : loved) {
            if (p.size() >= thing.getLoverNumber(p.endOne().x,p.endOne().y)) return LogicStatus.CONTRADICTION;
        }

        return LogicStatus.STYMIED;

    }
}
