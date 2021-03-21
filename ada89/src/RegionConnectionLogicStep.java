import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.EdgeContainer;
import grid.solverrecipes.singleloopflatten.EdgeState;

import java.util.ArrayList;
import java.util.List;

public class RegionConnectionLogicStep implements LogicStep<Board> {
    RegionInfo ri;
    public RegionConnectionLogicStep(RegionInfo regionInfo) { ri = regionInfo; }

    @Override public LogicStatus apply(Board thing) {
        RegionInfo.CountInfo ci = ri.getCounts(thing);
        
        if (ci.getPathCount() > 4) return LogicStatus.CONTRADICTION;
        if (ci.getPathCount() + ci.getUnknowns().size() < 4) return LogicStatus.CONTRADICTION;
        if (ci.getUnknowns().size() == 0) return LogicStatus.STYMIED;

        if (ci.getPathCount() == 4) {
            ci.getUnknowns().stream().forEach(cc->thing.setEdge(cc.x,cc.y,cc.d, EdgeState.WALL));
            return LogicStatus.LOGICED;
        }

        if (ci.getPathCount() + ci.getUnknowns().size() == 4) {
            ci.getUnknowns().stream().forEach(cc->thing.setEdge(cc.x,cc.y,cc.d,EdgeState.PATH));
            return LogicStatus.LOGICED;
        }

        return LogicStatus.STYMIED;
    }
}
