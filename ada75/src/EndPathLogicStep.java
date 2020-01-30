import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;
import grid.solverrecipes.singleloopflatten.EdgeState;
import org.jgraph.graph.Edge;

import java.util.ArrayList;
import java.util.List;

public class EndPathLogicStep implements LogicStep<Board> {
    int x;
    int y;

    public EndPathLogicStep(int x, int y) { this.x = x; this.y = y; }
    private void fill(Board b, List<Direction> dl, EdgeState es) { dl.stream().forEach(d->b.setEdge(x,y,d,es)); }

    @Override public LogicStatus apply(Board thing) {
        int wallcount = 0;
        int pathcount = 0;
        List<Direction> unknowns = new ArrayList<>();

        for (Direction d : Direction.orthogonals()) {
            switch(thing.getEdge(x,y,d)) {
                case WALL: ++wallcount; break;
                case PATH: ++pathcount; break;
                case UNKNOWN: unknowns.add(d); break;
            }
        }

        //  PATHv:        WALL:     0   1   2   3   4
        //
        //      0                   4S  3S  2S  1B  0y
        //      1                   3A  2A  1A  0z  X
        //      2                   2x  1x  0x  X   X
        //      3                   1x  0x  X   X   X
        //      4                   0x  X   X   X   X
        // (number in grid is unknown count)
        // X = not possible (we only have 4 items)
        // x = too many paths
        // y = path impossible
        // z = solution already found
        // A = single path known, all unknowns must be walls.
        // B = 3 walls known, unknown must be path
        // S = multiple possibilities for path

        if (pathcount > 1) return LogicStatus.CONTRADICTION;
        if (pathcount + unknowns.size() < 1) return LogicStatus.CONTRADICTION;
        if (unknowns.size() == 0) return LogicStatus.STYMIED;

        if (pathcount == 1) {
            fill(thing,unknowns,EdgeState.WALL);
            return LogicStatus.LOGICED;
        }

        if (wallcount == 3) {
            fill(thing,unknowns,EdgeState.PATH);
            return LogicStatus.LOGICED;
        }

        return LogicStatus.STYMIED;
    }
}
