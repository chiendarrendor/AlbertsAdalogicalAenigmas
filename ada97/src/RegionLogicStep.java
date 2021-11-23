import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;
import grid.puzzlebits.EdgeContainer;
import grid.solverrecipes.singleloopflatten.EdgeState;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

public class RegionLogicStep implements LogicStep<Board> {
    Set<Point> regionCells;
    Set<EdgeContainer.EdgeCoord> internalEdges = new HashSet<>();

    int regionSize;
    char regionid;
    public RegionLogicStep(char regionid, Board b, Set<Point> regionCells, int regionSize) {
        this.regionCells = regionCells;
        this.regionSize = regionSize;
        this.regionid = regionid;

        for (Point p : regionCells) {
            for (Direction d: Direction.orthogonals()) {
                Point op = d.delta(p,1);
                if (!regionCells.contains(op)) continue;
                internalEdges.add(EdgeContainer.getEdgeCoord(p.x,p.y,d));
            }
        }



    }

    // goal: the LONGEST path through this region must have a length EXACTLY of the given size
    // approach:
    // every cell in the region is either part of a path, or an unknown block
    // an unknown block is a set of cells connected by unknown edges
    //      an unknown block will connect to 0 or more paths via internal unknown edges
    // every path has, for each end:
    //      either a single known path out of the region, (CLOSED) or
    //      (OPEN) a list of 0 or more possible paths out of the region, and
    //      a list of the unknown blocks it could connect to
    //
    // a path's extended length = the number of cells it touches (+1 for each OPEN end with 0 paths out of the region)
    // if any path's extended length > regionSize, CONTRADICTION
    // make a graph where each vertex is a block or a path, and an edge connects two vertecies if there is an internal edge between them.
    // sum the cell counts of each connected set in this graph.   if no connected set has at least regionSize cells, CONTRADICTION





    @Override public LogicStatus apply(Board thing) {
        RegionParser rp = new RegionParser(thing,internalEdges, regionCells);

        if (!rp.isValid()) return LogicStatus.CONTRADICTION;

        if (rp.getMaxiumumPathLength() < regionSize) return LogicStatus.CONTRADICTION;
        if (rp.getMinimumMaximumPathLength() > regionSize) return LogicStatus.CONTRADICTION;

        LogicStatus result = LogicStatus.STYMIED;
        for (EdgeContainer.EdgeCoord ec : rp.closingEdges) {
            result = LogicStatus.LOGICED;
            thing.setEdge(ec.x,ec.y,ec.isV, EdgeState.WALL);
        }


        return result;
    }

    @Override public String toString() { return "RegionLogicStep for region " + regionid; }
}
