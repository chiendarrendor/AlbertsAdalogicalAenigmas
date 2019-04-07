import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;
import grid.solverrecipes.singleloopflatten.EdgeState;

import java.util.HashSet;
import java.util.Set;

public class CosmicCellLogicStep implements LogicStep<Board> {
    int x;
    int y;
    RegionStatus required;
    RegionStatus denied;
    char regionid;

    public CosmicCellLogicStep(int x, int y, RegionStatus required, char regionId) {
        this.x = x;
        this.y = y;
        this.required = required;
        this.denied = required == RegionStatus.DAY ? RegionStatus.NIGHT : RegionStatus.DAY;
        this.regionid = regionId;
    }

    @Override public LogicStatus apply(Board thing) {
        int pathcount = 0;
        int wallcount = 0;
        Set<Direction> unknowns = new HashSet<>();

        for (Direction d : Direction.orthogonals()) {
            switch(thing.getEdge(x,y,d)) {
                case UNKNOWN: unknowns.add(d); break;
                case PATH: ++pathcount; break;
                case WALL: ++wallcount; break;
            }
        }

        RegionStatus curstatus = thing.getRegionStatus(regionid);

        if (curstatus == RegionStatus.UNKNOWN) {
            if (wallcount > 2) {
                thing.setRegionStatus(regionid,denied);
                return LogicStatus.LOGICED;
            }
            if (pathcount > 0) {
                thing.setRegionStatus(regionid,required);
                return LogicStatus.LOGICED;
            }
            return LogicStatus.STYMIED;
        }

        if (curstatus == denied) {
            if (pathcount > 0) return LogicStatus.CONTRADICTION;
            if (unknowns.size() == 0) return LogicStatus.STYMIED;
            unknowns.stream().forEach(d->thing.setEdge(x,y,d, EdgeState.WALL));
            return LogicStatus.LOGICED;
        }

        // if curstatus == required
        if (wallcount > 2) return LogicStatus.CONTRADICTION;
        if (unknowns.size() == 0) return LogicStatus.STYMIED;
        if (wallcount == 2) {
            unknowns.stream().forEach(d->thing.setEdge(x,y,d,EdgeState.PATH));
            return LogicStatus.LOGICED;
        }

        return LogicStatus.STYMIED;
    }
}
