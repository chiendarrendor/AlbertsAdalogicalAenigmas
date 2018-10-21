import grid.logic.LogicStatus;
import grid.puzzlebits.Path.Path;
import grid.puzzlebits.Turns;
import grid.solverrecipes.singleloopflatten.BadMergeException;

import java.awt.LinearGradientPaint;
import java.awt.Point;

public class PathCorrectnessLogicStep implements grid.logic.LogicStep<Board> {

    private interface PathLogicStep {
        LogicStatus apply(Board thing, Path path);
    }

    private PathLogicStep[] steps = {
            // if dots are at both ends, they must have different numbers
            (board,path) -> {
                Point p1 = path.endOne();
                Point p2 = path.endTwo();

                if (!board.isDot(p1.x,p1.y)) return LogicStatus.STYMIED;
                if (!board.isDot(p2.x,p2.y)) return LogicStatus.STYMIED;
                if (board.getNumber(p1.x,p1.y) == board.getNumber(p2.x,p2.y)) return LogicStatus.CONTRADICTION;
                return LogicStatus.STYMIED;
            },
            // path may not bend more than once
            (board,path) -> {
                if (path.size() < 3) return LogicStatus.STYMIED;
                Path.Cursor cursor = path.getCursor(path.endOne().x,path.endOne().y);
                cursor.next();

                boolean bent = false;
                while(true) {
                    if (Turns.isBend(Turns.makeTurn(cursor.getPrev(),cursor.get(),cursor.getNext()))) {
                        if (bent) return LogicStatus.CONTRADICTION;
                        bent = true;
                    }
                    if (cursor.getNext().equals(path.endTwo())) return LogicStatus.STYMIED;
                    cursor.next();
                }
            }
    };

    @Override public LogicStatus apply(Board thing) {
        try {
            thing.getPaths().clean();
        } catch (BadMergeException bme) {
            return LogicStatus.CONTRADICTION;
        }

        LogicStatus result = LogicStatus.STYMIED;
        for (Path p : thing.getPaths()) {
            for (PathLogicStep pls : steps) {
                LogicStatus step = pls.apply(thing,p);
                if (step == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
                if (step == LogicStatus.LOGICED) result = LogicStatus.LOGICED;
            }

        }


        return result;
    }
}
