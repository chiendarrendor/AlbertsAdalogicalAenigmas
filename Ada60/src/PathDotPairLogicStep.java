import grid.logic.LogicStatus;
import grid.puzzlebits.Path.Path;
import grid.puzzlebits.Turns;

import java.awt.Point;

public class PathDotPairLogicStep implements grid.logic.LogicStep<Board> {
    @Override public LogicStatus apply(Board thing) {
        LogicStatus result = LogicStatus.STYMIED;
        for (Path p : thing.getPaths()) {
            LogicStatus stat = applyOnePath(thing,p);
            if (stat == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
            if (stat == LogicStatus.LOGICED) result = LogicStatus.LOGICED;
        }
        return result;
    }

    private LogicStatus applyOnePath(Board thing, Path p) {
        Point startPoint = p.endOne();
        Path.Cursor cursor = p.getCursor(startPoint.x,startPoint.y);
        // first, find the first dot.
        while(true) {
            if (thing.isDot(cursor.get().x,cursor.get().y)) break;
            if (!cursor.hasNext()) return LogicStatus.STYMIED; // a path with no dots is not inherently bad
            cursor.next();
            // a loop in the path with no dots _is_ bad.
            if (startPoint.equals(cursor.get())) return LogicStatus.CONTRADICTION;
        }

        Point firstDot = cursor.get();
        Point curDot = firstDot;
        if (!cursor.hasNext()) return LogicStatus.STYMIED; // if first dot is last cell, no useful info.
        cursor.next();

        int turncount = 0;
        while(true) {
            // invariant.  We have processed, and counted, everything up to, but not the current Cursor location
            if (thing.isDot(cursor.get().x,cursor.get().y)) {
                Point newDot = cursor.get();
                if (curDot.equals(newDot)) return LogicStatus.CONTRADICTION; // loop with one dot in it!
                // this ends a path segment.
                char curColor = thing.getColor(curDot.x,curDot.y);
                char newColor = thing.getColor(newDot.x,newDot.y);
                if (curColor == newColor && turncount > 0) return LogicStatus.CONTRADICTION;
                if (curColor != newColor && turncount != 1) return LogicStatus.CONTRADICTION;

                // if we survived that, let's set up for the next go.
                if (newDot.equals(firstDot)) break; // we've successfully looped.  any further processing would repeat.
                turncount = 0;
                curDot = newDot;
            } else {
                // non-loop space.
                // if we don't have a next, not only can we not calculate turning, but we don't care!
                if (!cursor.hasNext()) break;
                if (Turns.isBend(Turns.makeTurn(cursor.getPrev(),cursor.get(),cursor.getNext()))) ++turncount;
                if (turncount > 1) return LogicStatus.CONTRADICTION;
            }
            if (!cursor.hasNext()) break;
            cursor.next();
        }






        return LogicStatus.STYMIED;
    }
}
