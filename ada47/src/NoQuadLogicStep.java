import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

public class NoQuadLogicStep implements LogicStep<LogicBoard> {
    int x;
    int y;
    public NoQuadLogicStep(int x, int y) { this.x = x; this.y = y; }

    @Override public LogicStatus apply(LogicBoard thing) {
        int pathcount = 0;
        for (Direction d : Direction.orthogonals()) {
            if (thing.getEdge(x,y,d) == EdgeType.PATH) ++pathcount;
        }

        return pathcount > 2 ? LogicStatus.CONTRADICTION : LogicStatus.STYMIED;
    }
}
