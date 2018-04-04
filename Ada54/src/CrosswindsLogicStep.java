import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.util.HashMap;
import java.util.Map;

public class CrosswindsLogicStep extends CellLogicStep {
    Direction winddir;
    public CrosswindsLogicStep(int x, int y, Direction w) { super(x,y); winddir = w; }

    // if there is a single way in, and it is crossways to the wind direction,
    // it cannot go out the way it came.
    private Map<Direction,EdgeInfo> ins = new HashMap<>();
    public LogicStatus apply(Board thing) {
        divvy(thing,(d,ei)->synHas(d,ei,EdgeSynopsis.POSSIBLE_UNKNOWN,EdgeSynopsis.POSSIBLE_IN,
                EdgeSynopsis.USED_IN,EdgeSynopsis.USED_UNKNOWN),ins);
        if (ins.size() != 1) return LogicStatus.STYMIED;

        Map.Entry<Direction,EdgeInfo> ent = ins.entrySet().iterator().next();
        Direction ind = ent.getKey();
        EdgeInfo inei = ent.getValue();
        EdgeSynopsis inen = inei.getSynopsis(ind);

        LogicStatus result = LogicStatus.STYMIED;

        // if we find that it's the only possible in, then let's remove the outgoing.
        if (inen == EdgeSynopsis.POSSIBLE_UNKNOWN || inen == EdgeSynopsis.USED_UNKNOWN) {
            result = LogicStatus.LOGICED;
            inei.removeOutbound(ind);
        }

        // if we get here, then (ind,inei) is an incoming-only  edge.
        Direction opd = inei.pathDir();

        // opd can be four directions:
        // along wind (nothing to do)
        // left of/right of wind (what we want to look for)
        // opposing wind (which can't actually happen because that incoming direction has already been removed
        if (opd == winddir) return result;
        // if we get here, then the wind crosses, we can't get out the far side.
        EdgeInfo opedge = thing.getEdge(x,y,opd);
        if (opedge.canGo(opd)) {
            if (opedge.isUsed()) return LogicStatus.CONTRADICTION;
            result = LogicStatus.LOGICED;
            opedge.removeOutbound(opd);
        }

        // anything that follows from removing this outbound edge will be handled in another LogicStep

        return result;
    }

    public String toString() {
        return "CrosswindsLogicStep " + x + "," + y + ": " + winddir;
    }
}
