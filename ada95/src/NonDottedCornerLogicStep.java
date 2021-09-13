import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;
import grid.puzzlebits.EdgeContainer;

import java.util.Map;

public class NonDottedCornerLogicStep extends CornerLogicStep implements LogicStep<Board> {
    public NonDottedCornerLogicStep(Map<Direction, EdgeContainer.EdgeCoord> cornerEdges) { super(cornerEdges.values()); }

    @Override public LogicStatus apply(Board thing) {
        calculateEdgeState(thing);

        switch (wallcount) {
            case 4:
            case 3:
                return LogicStatus.CONTRADICTION;
            case 2:
                if (unknowns.size() == 0) return LogicStatus.STYMIED;
                unknowns.stream().forEach(e->thing.setEdge(e.x,e.y,e.isV,EdgeState.PATH));
                return LogicStatus.LOGICED;
            case 1:
                if (unknowns.size() == 0) return LogicStatus.CONTRADICTION;
                if (unknowns.size() == 1) {
                    unknowns.stream().forEach(e->thing.setEdge(e.x,e.y,e.isV,EdgeState.WALL));
                    return LogicStatus.LOGICED;
                }
                return LogicStatus.STYMIED;
            case 0:
                if (unknowns.size() == 1) {
                    unknowns.stream().forEach(e->thing.setEdge(e.x,e.y,e.isV,EdgeState.PATH));
                    return LogicStatus.LOGICED;
                }
                return LogicStatus.STYMIED;
            default: throw new RuntimeException("illegal value for wallcount");
        }
    }
}
