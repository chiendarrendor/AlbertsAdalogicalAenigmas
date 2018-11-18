import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import org.omg.PortableInterceptor.LOCATION_FORWARD;

import java.util.ArrayList;
import java.util.List;

public class RegionLogicStep implements LogicStep<Board> {
    int rid;

    private static class RegionEdgePair {
        public Region region;
        public Edge edge;
        public RegionEdgePair(Region r, Edge e) { region = r ; edge = e; }
    }


    public RegionLogicStep(int rid) { this.rid = rid; }

    @Override public LogicStatus apply(Board thing) {
        Region myr = thing.rg.regions.get(rid);

        List<RegionEdgePair> unknowns = new ArrayList<>();
        RegionEdgePair open = null;
        int wallcount = 0;

        for (int orid : myr.edges.keySet()) {
            Edge oe = myr.edges.get(orid);
            Region or = thing.rg.regions.get(orid);

            switch(oe.getState()) {
                case UNKNOWN:
                    unknowns.add(new RegionEdgePair(or,oe));
                    break;
                case OPEN:
                    if (open != null) return LogicStatus.CONTRADICTION; // multiple open edges is bad
                    open = new RegionEdgePair(or,oe);
                    break;
                case CLOSED:
                    ++wallcount;
                    break;
            }
        }

        // if we get here, we have at most one open edge.

        // no way to get an open edge is bad.
        if (open == null && unknowns.size() == 0) return LogicStatus.CONTRADICTION;

        // exactly one way to get an open edge is good
        if (open == null && unknowns.size() == 1) {
            RegionEdgePair newopen = unknowns.get(0);
            thing.setRegionEdge(newopen.edge.regionid1,newopen.edge.regionid2,EdgeState.OPEN);
            myr.setPair(newopen.region.regionid);
            return LogicStatus.LOGICED;
        }

        // if there are multiple ways out, we don't know.
        if (open == null) return LogicStatus.STYMIED;

        // if we get here, open is not null!
        LogicStatus result = LogicStatus.STYMIED;

        if (!myr.isPaired()) {
            result = LogicStatus.LOGICED;
            myr.setPair(open.region.regionid);
        }

        for (RegionEdgePair rep : unknowns) {
            thing.setRegionEdge(rep.edge.regionid1,rep.edge.regionid2,EdgeState.CLOSED);
            result = LogicStatus.LOGICED;
        }

        return result;
    }
}
