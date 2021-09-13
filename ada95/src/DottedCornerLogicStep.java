import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;
import grid.puzzlebits.EdgeContainer;

import java.util.Map;

public class DottedCornerLogicStep extends CornerLogicStep implements LogicStep<Board>  {
    public DottedCornerLogicStep(Map<Direction, EdgeContainer.EdgeCoord> cornerEdges) { super(cornerEdges.values()); }

    @Override public LogicStatus apply(Board thing) {
        calculateEdgeState(thing);
        if (wallcount + unknowns.size() < 3) return LogicStatus.CONTRADICTION;
        if (unknowns.size() == 0) return LogicStatus.STYMIED;
        if (wallcount + unknowns.size() == 3) {
            unknowns.stream().forEach(e->thing.setEdge(e.x,e.y,e.isV,EdgeState.WALL));
            return LogicStatus.LOGICED;
        }
        return LogicStatus.STYMIED;
    }
}
