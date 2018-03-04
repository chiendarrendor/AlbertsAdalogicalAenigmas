import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.*;
import java.util.stream.Stream;

public class CircleLogicStep implements LogicStep<Board>
{
    Point p;
    public CircleLogicStep(Point p) { this.p = p; }

    public LogicStatus apply(Board thing)
    {
        Circle c = thing.circles.get(p);
        if (c.isLocked()) return LogicStatus.STYMIED;
        int count = Stream.of(c.paths.toArray(new Path[0])).mapToInt((p)->{
            if (!c.placeable(p)) {
                c.removeOnePath(p);
                return 1;
            }
            return 0;
        }).sum();

        if (c.paths.size() == 0) return LogicStatus.CONTRADICTION;

        LogicStatus result = LogicStatus.STYMIED;
        if (count > 0) result = LogicStatus.LOGICED;

        if (c.paths.size() == 1) {
            c.lock();
            result = LogicStatus.LOGICED;
        }

        return result;
    }
}
