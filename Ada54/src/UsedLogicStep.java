import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.util.HashMap;
import java.util.Map;

public class UsedLogicStep extends CellLogicStep {
    EdgeSynopsis direction;
    EdgeSynopsis otherdirection;
    EdgeSynopsis possible;
    EdgeSynopsis otherpossible;

    private void remove(EdgeInfo ei,Direction d) {
        if (direction == EdgeSynopsis.USED_IN) {
            ei.removeInbound(d);
        } else {
            ei.removeOutbound(d);
        }
    }

    public UsedLogicStep(int x, int y,EdgeSynopsis direction) {
        super(x,y);
        if (direction != EdgeSynopsis.USED_IN && direction != EdgeSynopsis.USED_OUT) {
            throw new RuntimeException("UsedLogicStep only takes USED_specific");
        }
        this.direction = direction;
        otherdirection = direction == EdgeSynopsis.USED_IN ? EdgeSynopsis.USED_OUT : EdgeSynopsis.USED_IN;
        possible = direction == EdgeSynopsis.USED_IN ? EdgeSynopsis.POSSIBLE_IN : EdgeSynopsis.POSSIBLE_OUT;
        otherpossible = direction == EdgeSynopsis.USED_IN ? EdgeSynopsis.POSSIBLE_OUT : EdgeSynopsis.POSSIBLE_IN;
    }

    private Map<Direction,EdgeInfo> usedin = new HashMap<>();
    private Map<Direction,EdgeInfo> usedother = new HashMap<>();
    private Map<Direction,EdgeInfo> nonusednonwalls = new HashMap<>();
    public LogicStatus apply(Board thing) {
        if (thing.isCellComplete(x,y)) return LogicStatus.STYMIED;

        divvy(thing,(d,ei) -> ei.getSynopsis(d) == direction,usedin);
        if (usedin.size() == 0) return LogicStatus.STYMIED;
        if (usedin.size() > 1) return LogicStatus.CONTRADICTION;

        // if we get here we have exactly one used <direction>, which is what we want.
        divvy(thing,(d,ei) -> synHas(d,ei,EdgeSynopsis.USED_UNKNOWN,otherdirection),usedother);
        if (usedother.size() > 1) return LogicStatus.CONTRADICTION;

        if (usedother.size() == 1) {
            Map.Entry<Direction, EdgeInfo> ment = usedother.entrySet().iterator().next();
            Direction ud = ment.getKey();
            EdgeInfo uei = ment.getValue();
            if (uei.getSynopsis(ud) == EdgeSynopsis.USED_UNKNOWN) {
                remove(uei,ud);
                thing.useEdge(x,y,ud);
            }

            divvy(thing, (d, ei) -> !ei.isUsed() && !ei.isWall(), nonusednonwalls);
            nonusednonwalls.values().stream().forEach((ei) -> ei.clear());
            thing.setCellComplete(x, y);
            return LogicStatus.LOGICED;
        }

        // so if we get here, we have exactly one used <direction> and no other used.
        LogicStatus result = LogicStatus.STYMIED;
        // no other direction should be <direction>
        divvy(thing,(d,ei)-> synHas(d,ei,EdgeSynopsis.POSSIBLE_UNKNOWN,possible),nonusednonwalls);
        if (nonusednonwalls.size() > 0) result = LogicStatus.LOGICED;
        nonusednonwalls.entrySet().stream().forEach((x)->remove(x.getValue(),x.getKey()));

        // lets look at possibles in the other direction...
        divvy(thing,(d,ei)->synHas(d,ei,EdgeSynopsis.POSSIBLE_UNKNOWN,otherpossible),nonusednonwalls);
        if (nonusednonwalls.size() == 0) return LogicStatus.CONTRADICTION;
        if (nonusednonwalls.size() > 1) return result;
        // if we get here, we have exactly one!  it's our other direction!
        Map.Entry<Direction,EdgeInfo> ent = nonusednonwalls.entrySet().iterator().next();
        Direction d = ent.getKey();
        EdgeInfo ei = ent.getValue();

        thing.useEdge(x,y,d);
        thing.setCellComplete(x,y);

        return LogicStatus.LOGICED;
    }
}
