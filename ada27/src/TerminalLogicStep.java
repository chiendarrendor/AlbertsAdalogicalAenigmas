import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class TerminalLogicStep implements LogicStep<Board> {
    int x;
    int y;
    public TerminalLogicStep(Point p) { x = p.x; y = p.y; }

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

        // a terminal cell must have exactly one path
        //  -> pathcount  v wallcount
        //      0   1   2   3   4
        //  0   ?   W   B   B   B
        //  1   ?   W   B   B   X
        //  2   ?   W   B   X   X
        //  3   P   G   X   X   X
        //  4   B   X   X   X   X
        //
        // X = not possible
        // G = good
        // B = bad
        // W = good, but make all unknowns walls
        // P = Good, but make all unknowns paths

        // all the 'B's
        if (pathcount > 1) return LogicStatus.CONTRADICTION;
        if (wallcount == 4) return LogicStatus.CONTRADICTION;
        // all the 'G's
        if (unknowns.size() == 0) return LogicStatus.STYMIED;

        // all the 'W's
        if (pathcount == 1) {
            unknowns.stream().forEach(d->thing.setWall(x,y,d));
            return LogicStatus.LOGICED;
        }

        if (wallcount == 3) {
            unknowns.stream().forEach(d->thing.setPath(x,y,d));
            return LogicStatus.LOGICED;
        }

        return LogicStatus.STYMIED;
    }


}
