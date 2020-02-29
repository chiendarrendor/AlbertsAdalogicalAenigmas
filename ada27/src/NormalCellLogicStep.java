import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class NormalCellLogicStep implements LogicStep<Board> {
    int x;
    int y;
    public NormalCellLogicStep(Point p) { x = p.x; y = p.y; }


    @Override public LogicStatus apply(Board thing) {

        int pathcount = 0;
        int wallcount = 0;
        List<Direction> unknowns = new ArrayList<>();

        for (Direction d : Direction.orthogonals()) {
            switch(thing.getEdge(x,y,d)) {
                case UNKNOWN: unknowns.add(d); break;
                case PATH: ++pathcount; break;
                case WALL: ++wallcount; break;
            }
        }

        // a normal cell may have exactly 0, or exactly 2 paths.
        //  -> pathcount  v wallcount
        //      0   1   2   3   4
        //  0   ?   ?   W   B   B
        //  1   ?   ?   W   B   X
        //  2   ?   P   G   X   X
        //  3   W   B   X   X   X
        //  4   G   X   X   X   X
        //
        // X = not possible
        // G = good
        // B = bad
        // W = good, but make all unknowns walls
        // P = Good, but make all unknowns paths

        // all the 'B's
        if (pathcount > 2) return LogicStatus.CONTRADICTION;
        if (pathcount == 1 && wallcount == 3) return LogicStatus.CONTRADICTION;
        // all the 'G's
        if (unknowns.size() == 0) return LogicStatus.STYMIED;

        // all the 'W's
        if (pathcount == 2 || wallcount == 3) {
            unknowns.stream().forEach(d->thing.setWall(x,y,d));
            return LogicStatus.LOGICED;
        }

        // all the 'P's
        if (pathcount == 1 && wallcount == 2) {
            unknowns.stream().forEach(d->thing.setPath(x,y,d));
            return LogicStatus.LOGICED;
        }

        // all the '?'s
        return LogicStatus.STYMIED;
    }
}
