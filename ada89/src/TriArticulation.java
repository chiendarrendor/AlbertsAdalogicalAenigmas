
// determines if this collection of cells has a tri-articulation
// (throw an exception if it hss a quad-articulation or more than one tri
// if it does have a tri, we need the following stuff easy to get:
// 1) list (0-1) of all region-leaving edges of the articulation point
// 2) for each sub-region of the articulation point:
//    a) the internal edge(s) between sub-region and articulation point
//    b) all region-leaving edges of that sub-region


import grid.graph.GridGraph;
import grid.puzzlebits.Direction;
import grid.puzzlebits.EdgeContainer;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// the 'tripoint' is the unique, if present, cell of a region that, if removed, will break the region
// into exactly three 'sub-regions'.
// the 'outs' of a sub-region are those edges that go from this region to another one
// the 'ins' of a sub-region are those edges that go back to the tripoint
// the 'leftovers' is that edge (if present) of the tripoint that go out of the region
public class TriArticulation {

    private class SubRegionPair {
        Collection<EdgeContainer.CellCoord> outs;
        Collection<EdgeContainer.CellCoord> ins;

        public SubRegionPair(Collection<EdgeContainer.CellCoord> ins, Collection<EdgeContainer.CellCoord> outs) {
            this.ins = ins;
            this.outs = outs;
        }
    }

    private Point tripoint;
    private List<SubRegionPair> subregions = new ArrayList<>();
    private List<EdgeContainer.CellCoord> leftovers = new ArrayList<>();

    private void addSubRegion(Collection<EdgeContainer.CellCoord> ins, Collection<EdgeContainer.CellCoord> outs) {
        subregions.add(new SubRegionPair(ins,outs));
    }
    private void addLeftover(EdgeContainer.CellCoord leftover) { leftovers.add(leftover); }

    private TriArticulation(Point tripoint) { this.tripoint = tripoint; }

    public Collection<EdgeContainer.CellCoord> getSubRegionOutEdges(int index) {
        return subregions.get(index).outs;
    }

    public Collection<EdgeContainer.CellCoord> getSubRegionInEdges(int index) {
        return subregions.get(index).ins;
    }

    public Collection<EdgeContainer.CellCoord> getLeftovers() {
        return leftovers;
    }


    public static TriArticulation generate(Set<Point> cells) {
        GridGraph basegg = new GridGraph(new MyGridReference(cells));
        if (!basegg.isConnected()) throw new RuntimeException("Region must be connected!");
        Set<Point> arties = basegg.getArticulationPoints();

        Point tripoint = null;
        List<Set<Point>> triconsets = null;

        for (Point p : arties) {
            GridGraph agg = new GridGraph(new MyGridReference(cells,p));
            List<Set<Point>> consets = agg.connectedSets();
            if (consets.size() < 2 || consets.size() > 3)
                throw new RuntimeException("Articulation Point of Region shouldn't have " + consets.size() + " connected sets!");
            if (consets.size() == 2) continue;
            if (tripoint != null) throw new RuntimeException("Region has multiple triarticulation points!");
            tripoint = p;
            triconsets = consets;
        }
        if (tripoint == null) return null;
        // if we get here, we have exactly one tri-articulation point
        TriArticulation result = new TriArticulation(tripoint);
        Set<Direction> leftovers = new HashSet<Direction>();
        Arrays.stream(Direction.orthogonals()).forEach(d->leftovers.add(d));

        for (Set<Point> conset: triconsets) {
            Set<EdgeContainer.CellCoord> outs = new HashSet<>();
            Set<EdgeContainer.CellCoord> ins = new HashSet<>();
            for (Point p : conset) {
                for (Direction d : Direction.orthogonals()) {
                    Point indir = d.delta(p, 1);
                    if (conset.contains(indir)) continue;
                    if (indir.equals(tripoint)) {
                        ins.add(new EdgeContainer.CellCoord(p.x, p.y, d));
                        leftovers.remove(d.getOpp());
                    } else {
                        outs.add(new EdgeContainer.CellCoord(p.x, p.y, d));
                    }
                }
            }
            result.addSubRegion(ins,outs);
        }

        for (Direction d : leftovers) {
            result.addLeftover(new EdgeContainer.CellCoord(tripoint.x,tripoint.y,d));
        }
        return result;
    }
}
