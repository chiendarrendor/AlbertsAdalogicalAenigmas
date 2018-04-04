import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.util.*;

public abstract class CellLogicStep implements LogicStep<Board> {
    protected int x;
    protected int y;

    public CellLogicStep(int x, int y) {
        this.x = x;
        this.y = y;
    }

    protected interface DivvyI {
        boolean is(Direction d, EdgeInfo ei);
    }

    protected void divvy(Board thing, DivvyI is, Map<Direction, EdgeInfo> map) {
        map.clear();
        Arrays.stream(Direction.orthogonals())
                .filter((d) -> is.is(d, thing.getEdge(x, y, d)))
                .forEach((d) -> map.put(d, thing.getEdge(x, y, d)));
    }

    static boolean synHas(Direction d,EdgeInfo ei,EdgeSynopsis ... syns) {
        return Arrays.stream(syns).anyMatch((syn)->ei.getSynopsis(d) == syn);
    }
}

