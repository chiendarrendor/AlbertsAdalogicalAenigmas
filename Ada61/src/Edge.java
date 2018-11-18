import grid.puzzlebits.Direction;
import grid.puzzlebits.EdgeContainer;

import java.util.HashSet;
import java.util.Set;

public class Edge {
    int regionid1;
    int regionid2;
    private EdgeState state;
    Set<EdgeContainer.CellCoord> edges = new HashSet<>();
    RegionPair pair = null;

    public Edge(int rid1,int rid2) { regionid1 = rid1; regionid2 = rid2; state = EdgeState.UNKNOWN; }
    public Edge(Edge right) {
        regionid1 = right.regionid1;
        regionid2 = right.regionid2;
        state = right.state;
        edges = right.edges; // once we construct the first graph, the set of real edges between regions is invariant.
        pair = right.pair; // pairs are also invariant.
    }

    public void addEdge(int x,int y,Direction d) { edges.add(new EdgeContainer.CellCoord(x,y,d)); }
    public void setPair(RegionPair rp) { pair = rp; }

    public EdgeState getState() { return state; }
    public void setState(EdgeState newstate, boolean imAllowed) {
        if (!imAllowed) {
            throw new RuntimeException("You're not allowed to change state on edge!");
        }
        state = newstate;
    }


}
