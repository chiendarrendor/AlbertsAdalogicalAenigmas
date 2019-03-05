import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;

public class ExclusionZoneLogicStep implements LogicStep<Board> {
    int regionid;
    public ExclusionZoneLogicStep(Region r) { regionid = r.getId(); }

    @Override public LogicStatus apply(Board thing) {
        Region r = thing.getRegionById(regionid);
        if (!r.isDone()) return LogicStatus.STYMIED;

        LogicStatus result = LogicStatus.STYMIED;
        for (Point p : r.getAdjacents(thing,false)) {
            if(thing.exclusionZones.getCell(p.x,p.y).contains(r.getActualSize())) {
                result = LogicStatus.LOGICED;
                thing.exclusionZones.getCell(p.x,p.y).remove(r.getActualSize());
            }
        }

        return result;
    }
}
