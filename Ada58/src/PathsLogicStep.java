import grid.logic.LogicStatus;
import grid.puzzlebits.Path.Path;

import java.awt.Point;

public class PathsLogicStep implements grid.logic.LogicStep<Board> {
    @Override public LogicStatus apply(Board thing) {
        try {
            thing.paths.clean();
        } catch(BadMergeException bme) {
            return LogicStatus.CONTRADICTION;
        }


        LogicStatus result = LogicStatus.STYMIED;

        for (Path p : thing.paths) {
            LogicStatus presult = applyToPath(thing, p);
            if (presult == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
            if (presult == LogicStatus.LOGICED) result = LogicStatus.LOGICED;
        }

        return result;
    }

    // validate a path.  due to all spaces having two paths, and terminals having one path,
    // and the path merge logic rejecting loops
    // we are guaranteed that every path will ultimately end in a terminal and don't have to check
    // the only thing we have to check is that the path is either monotonically increasing
    // or decreasing.

    public LogicStatus applyToPath(Board thing, Path path) {
        // the most recently seen number.  If we've never seen a number, this is -1
        int lastseen = -1;
        // the delta of the path (+1 if we are increasing, -1 if decreasing, 0 if a second number is not seen)
        int delta = 0;

        for (Point p : path) {
            if (!thing.hasNumber(p.x,p.y)) continue;
            int curnum = thing.getNumber(p.x,p.y);

            if (lastseen == -1) {
                lastseen = curnum;
                continue;
            }

            if (delta == 0) {
                if (curnum > lastseen) {
                    delta = 1;
                } else if (curnum < lastseen) {
                    delta = -1;
                } else {
                    return LogicStatus.CONTRADICTION;
                }
            }

            if (curnum != lastseen + delta) return LogicStatus.CONTRADICTION;
            lastseen = curnum;
        }
        return LogicStatus.STYMIED;
    }
}
