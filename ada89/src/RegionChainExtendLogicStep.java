// for any region that has exactly two external edges as PATH where the two edges are connected
// within the region:
// a) all remaining cells in region must be connected
// b) all articulation points of rank 2 must connect their subregions

import grid.graph.GridGraph;
import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;
import grid.puzzlebits.EdgeContainer;
import grid.solverrecipes.singleloopflatten.EdgeState;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RegionChainExtendLogicStep  implements LogicStep<Board> {
    RegionInfo ri;
    public RegionChainExtendLogicStep(RegionInfo regionInfo) { ri = regionInfo; }

    @Override public LogicStatus apply(Board thing) {
        RegionInfo.CountInfo ci = ri.getCounts(thing);
        if (ci.getPathCount() != 2) return LogicStatus.STYMIED;

        Set<Point> notincon = new HashSet<>();
        notincon.addAll(ri.cells);

        Iterator<EdgeContainer.CellCoord> it = ci.getPaths().iterator();
        EdgeContainer.CellCoord from = it.next();
        EdgeContainer.CellCoord to = it.next();
        while(true) {
            Point p = new Point(from.x,from.y);
            // this would be the detection of a loop
            if (!notincon.contains(p)) return LogicStatus.CONTRADICTION;
            notincon.remove(p);
            Direction outdir = null;
            for (Direction d : Direction.orthogonals()) {
                if (d == from.d) continue;
                switch(thing.getEdge(p.x,p.y,d)) {
                    case WALL: break;
                    case UNKNOWN: break;
                    case PATH:
                        if (outdir != null) return LogicStatus.CONTRADICTION;
                        outdir = d;
                        break;
                }
            }
            if (outdir == null) return LogicStatus.STYMIED;
            EdgeContainer.CellCoord newout = new EdgeContainer.CellCoord(p.x,p.y,outdir);
            if (newout.equals(to)) break;
            Point np = outdir.delta(p,1);
            from = new EdgeContainer.CellCoord(np.x,np.y,outdir.getOpp());
        }
        // if we get here, we found an unbroken chain from from to to, and have removed from
        // notincon all cells mentioned.
        GridGraph gg = new GridGraph(new MyGridReference(notincon));
        if (!gg.isConnected()) return LogicStatus.CONTRADICTION;

        LogicStatus result = LogicStatus.STYMIED;

        for (Point artie : gg.getArticulationPoints()) {
            GridGraph subgg = new GridGraph(new MyGridReference(notincon,artie));
            List<Set<Point>> consets = subgg.connectedSets();
            if (consets.size() > 2) continue;

            Map<Integer,List<Direction>> mapdirs = new HashMap<>();
            List<Direction> leftovers = new ArrayList<>();
            for (Direction d: Direction.orthogonals()) {
                boolean found = false;
                for (int idx = 0 ; idx < consets.size() ; ++idx) {
                    Point ap = d.delta(artie,1);
                    if (consets.get(idx).contains(ap)) {
                        if (!mapdirs.containsKey(idx)) mapdirs.put(idx,new ArrayList<>());
                        mapdirs.get(idx).add(d);
                        found = true;
                        break;
                    }
                }
                if (!found) leftovers.add(d);
            }

            for (Direction d : leftovers) {
                if (thing.getEdge(artie.x,artie.y,d) == EdgeState.UNKNOWN) {
                    result = LogicStatus.LOGICED;
                    thing.setEdge(artie.x,artie.y,d,EdgeState.WALL);
                }
            }

            for (int key : mapdirs.keySet()) {
                List<Direction> dirs = mapdirs.get(key);
                if (dirs.size() > 1) continue;
                Direction d = dirs.get(0);
                if (thing.getEdge(artie.x,artie.y,d) == EdgeState.UNKNOWN) {
                    result = LogicStatus.LOGICED;
                    thing.setEdge(artie.x,artie.y,d,EdgeState.PATH);
                }
            }
        }


        return result;
    }
}
