import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import sun.rmi.runtime.Log;

public class EdgeLogicStep implements LogicStep<Board> {
    int rid1;
    int rid2;
    public EdgeLogicStep(int regionid1, int regionid2) { rid1 = regionid1; rid2 = regionid2; }

    @Override public LogicStatus apply(Board thing) {
        LogicStatus result = LogicStatus.STYMIED;
        Edge e = thing.rg.regions.get(rid1).edges.get(rid2);

        if (e.pair.isRectangular()) {
            switch(e.getState()) {
                case UNKNOWN: thing.setRegionEdge(e.regionid1,e.regionid2,EdgeState.CLOSED); result =  LogicStatus.LOGICED; break;
                case OPEN: return LogicStatus.CONTRADICTION;
                case CLOSED: break;
            }
        }

        if (e.getState() == EdgeState.UNKNOWN || e.getState() == EdgeState.OPEN) return LogicStatus.STYMIED;
        // if we get here, we're looking at a closed edge
        Region r1 = thing.rg.regions.get(rid1);
        Region r2 = thing.rg.regions.get(rid2);

        if (!r1.isPaired()) return result;
        if (!r2.isPaired()) return result;

        // if we are here, then we are the wall between two paired regions.
        // they have to have different shapes.

        if (r1.pair.sameShape(r2.pair)) return LogicStatus.CONTRADICTION;

        return result;
    }
}
