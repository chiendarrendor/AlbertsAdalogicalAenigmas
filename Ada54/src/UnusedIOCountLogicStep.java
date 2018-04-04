import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.util.HashMap;
import java.util.Map;

public class UnusedIOCountLogicStep extends CellLogicStep {
    public UnusedIOCountLogicStep(int x, int y) {
        super(x,y);
    }

    private Map<Direction,EdgeInfo> useds = new HashMap<>();
    private Map<Direction,EdgeInfo> ins = new HashMap<>();
    private Map<Direction,EdgeInfo> outs = new HashMap<>();
    private Map<Direction,EdgeInfo> walls = new HashMap<>();

    public LogicStatus apply(Board thing) {
        if (thing.isCellComplete(x,y)) return LogicStatus.STYMIED;
        divvy(thing,(d,ei) -> ei.isUsed(),useds);
        if (useds.size() > 0) return LogicStatus.STYMIED;
        divvy(thing,(d,ei)->ei.isWall(),walls);
        if (walls.size() > 2) return LogicStatus.STYMIED;

        // if we get here, we have no used, we have no more than 2 walls,
        // which means that we have at least two edges of POSSIBLE_X

        divvy(thing,(d,ei)-> synHas(d,ei,EdgeSynopsis.POSSIBLE_UNKNOWN,EdgeSynopsis.POSSIBLE_IN),ins);
        divvy(thing,(d,ei)-> synHas(d,ei,EdgeSynopsis.POSSIBLE_UNKNOWN,EdgeSynopsis.POSSIBLE_OUT),outs);

        if (ins.size() == 0) {
            outs.values().stream().forEach((x)->x.clear());
            thing.setCellComplete(x,y);
            return LogicStatus.LOGICED;
        }

        if (outs.size() == 0) {
            ins.values().stream().forEach((x)->x.clear());
            thing.setCellComplete(x,y);
            return LogicStatus.LOGICED;
        }

        // if we get here, both ins and outs have at least one direction.  If either side is solo,
        // and that solo is an unknown, then it has to be that direction.
        LogicStatus result = LogicStatus.STYMIED;
        if (ins.size() == 1) {
            Map.Entry<Direction,EdgeInfo> ent = ins.entrySet().iterator().next();
            Direction d = ent.getKey();
            EdgeInfo ei = ent.getValue();
            if (ei.getSynopsis(d) == EdgeSynopsis.POSSIBLE_UNKNOWN) {
                result = LogicStatus.LOGICED;
                ei.removeOutbound(d);
            }
        }

        if (outs.size() == 1) {
            Map.Entry<Direction,EdgeInfo> ent = outs.entrySet().iterator().next();
            Direction d = ent.getKey();
            EdgeInfo ei = ent.getValue();
            if (ei.getSynopsis(d) == EdgeSynopsis.POSSIBLE_UNKNOWN) {
                result = LogicStatus.LOGICED;
                ei.removeInbound(d);
            }
        }

        return result;
    }
}
