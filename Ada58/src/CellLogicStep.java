import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.util.HashSet;
import java.util.Set;

public class CellLogicStep implements LogicStep<Board> {
    int x;
    int y;
    int tpc;


    public CellLogicStep(int x, int y, boolean terminal) {
        this.x = x;
        this.y = y;
        this.tpc = terminal ? 1 : 2;
    }

    private Set<Direction> unknowns = new HashSet<>();
    @Override public LogicStatus apply(Board thing) {
        int pathcount = 0;
        int wallcount = 0;
        unknowns.clear();

        for (Direction d : Direction.orthogonals()) {
            switch(thing.getEdge(x,y,d)) {
                case WALL: ++wallcount; break;
                case PATH: ++pathcount; break;
                case UNKNOWN: unknowns.add(d); break;
            }
        }

        if (pathcount > tpc) return LogicStatus.CONTRADICTION;
        if (pathcount + unknowns.size() < tpc) return LogicStatus.CONTRADICTION;
        if (unknowns.size() == 0) return LogicStatus.STYMIED;

        if (pathcount == tpc) {
            unknowns.stream().forEach(d->thing.setEdge(x,y,d,EdgeState.WALL));
            return LogicStatus.LOGICED;
        }

        if (pathcount + unknowns.size() == tpc) {
            unknowns.stream().forEach(d->thing.setEdge(x,y,d,EdgeState.PATH));
            return LogicStatus.LOGICED;
        }

        return LogicStatus.STYMIED;
    }
}
