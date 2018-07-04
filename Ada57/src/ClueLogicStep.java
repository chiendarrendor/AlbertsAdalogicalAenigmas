import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClueLogicStep implements LogicStep<Board> {
    int x;
    int y;
    int dist;
    public ClueLogicStep(int x, int y, int dist) { this.x = x; this.y = y; this.dist = dist; }


    Map<Direction,List<Point>> paths = new HashMap<>();
    @Override public LogicStatus apply(Board thing) {
        paths.clear();
        // check to see if we've gone over
        int mincount = 0;
        for (Direction d : Direction.orthogonals()) {
            List<Point> path = Tracer.trace(thing,x,y,d,false,false);
            if (path.size() > 0) paths.put(d,path);
            mincount += path.size();
        }

        if (mincount > dist) return LogicStatus.CONTRADICTION;

        paths.clear();
        int maxcount = 0;
        for (Direction d : Direction.orthogonals()) {
            List<Point> path = Tracer.trace(thing,x,y,d,true,false);
            if (path.size() > 0) paths.put(d,path);
            maxcount += path.size();
        }

        if (maxcount < dist) return LogicStatus.CONTRADICTION;


        return LogicStatus.STYMIED;
    }
}
