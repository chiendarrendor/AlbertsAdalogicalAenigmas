import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.EdgeContainer;
import grid.solverrecipes.singleloopflatten.EdgeState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// Deep analysis of articulation points of regions in this puzzle
// (articulation point: defined as a vertex of a graph that, if removed, disconnets a connected graph)
// 1) if an articulation point breaks the region into more than 3 pieces, the puzzle is insolvalble
//    -- articulation point can only connect two of the disconnected pieces, leaving not enough in/out edges
//       for the other two disconnected pieces
// 2) if an articulation point breaks the region into more than two pieces, path through must connect two
//    of those pieces
//    -- otherwise there will be two disconnected pieces with not enough in/out edges
//    -- simple side-effect:  if an edge of a tri-articulation point leaves the region, path can't go that way
// 3) a region cannot have more than 1 tri-articulation point
//    -- any path through articulation points will end up leaving not enough in/out edges for at least one
//       sub-region
// 4) a sub-region of a tri-articulation point may not have more than two paths in/out of the region
// 5) if a sub-region of a tri-articulation point has two paths in/out, then the tri-articulation point
//    must wall that sub-region and connect the other two
// 6) if a sub-region as only 1 path in/out, it must connect to the articulation point
// 7) if a sub-region has 0 paths in/out, we're stuck (even if we connect to articulation point twice,
//    that closes off loop
// 8) the articulation point _must_ join two different regions, or the two not joined will need 2
//    out edges each, leaving the third region with no outs
public class RegionTriArticulationPointLogicStep implements LogicStep<Board> {
    TriArticulation tri;
    public RegionTriArticulationPointLogicStep(TriArticulation tri) { this.tri = tri; }

    @Override public LogicStatus apply(Board thing) {
        LogicStatus result = LogicStatus.STYMIED;

        for (int idx = 0 ; idx < 3 ; ++idx) {
            Collection<EdgeContainer.CellCoord> outs = tri.getSubRegionOutEdges(idx);
            Collection<EdgeContainer.CellCoord> ins = tri.getSubRegionInEdges(idx);
            int outpaths = 0;
            int outwalls = 0;
            List<EdgeContainer.CellCoord> outunknowns = new ArrayList<>();
            for (EdgeContainer.CellCoord cc : outs) {
                switch(thing.getEdge(cc.x,cc.y,cc.d)) {
                    case UNKNOWN: outunknowns.add(cc); break;
                    case PATH: ++outpaths; break;
                    case WALL: ++outwalls; break;
                }
            }
            if (outpaths > 2) return LogicStatus.CONTRADICTION;
            if (outpaths + outunknowns.size() == 0) return LogicStatus.CONTRADICTION;

            int inpaths = 0;
            int inwalls = 0;
            List<EdgeContainer.CellCoord> inunknowns = new ArrayList<>();
            for (EdgeContainer.CellCoord cc : ins) {
                switch(thing.getEdge(cc.x,cc.y,cc.d)) {
                    case UNKNOWN: inunknowns.add(cc); break;
                    case PATH: ++inpaths; break;
                    case WALL: ++inwalls; break;
                }
            }

            if (inpaths == 2) return LogicStatus.CONTRADICTION;



            for (EdgeContainer.CellCoord leftover : tri.getLeftovers()) {
                switch(thing.getEdge(leftover.x,leftover.y,leftover.d)) {
                    case PATH: return LogicStatus.CONTRADICTION;
                    case WALL: break;
                    case UNKNOWN:
                        thing.setEdge(leftover.x,leftover.y,leftover.d, EdgeState.WALL);
                        result = LogicStatus.LOGICED;
                        break;
                }
            }

            // if we get here, we have no more than 2 out paths and no more than 1 in path
            if (outpaths == 2) {
                if (outunknowns.size() > 0) {
                    outunknowns.stream().forEach(cc->thing.setEdge(cc.x,cc.y,cc.d,EdgeState.WALL));
                    result = LogicStatus.LOGICED;
                }
                for(EdgeContainer.CellCoord inpath : ins) {
                    switch (thing.getEdge(inpath.x,inpath.y,inpath.d)) {
                        case PATH: return LogicStatus.CONTRADICTION;
                        case WALL: break;
                        case UNKNOWN:
                            result = LogicStatus.LOGICED;
                            thing.setEdge(inpath.x,inpath.y,inpath.d,EdgeState.WALL);
                    }
                }
            }




        }


        return result;
    }
}
