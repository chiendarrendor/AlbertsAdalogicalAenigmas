import grid.puzzlebits.Direction;
import grid.puzzlebits.EdgeContainer;
import grid.solverrecipes.singleloopflatten.EdgeState;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

// this class will produce a list of all UNKNOWN edges of the given board, in the following order:
// 1) any edge that is at the other end of path from a terminal
// 2) any edge that is at the end of a path
// 3) any edge that comes out of a terminal
// 4) all other edges
public class EdgeOrdering implements Iterable<EdgeContainer.EdgeCoord> {
    public List<EdgeContainer.EdgeCoord> edgeorder = new ArrayList<>();
    public Set<EdgeContainer.EdgeCoord> edgesseen = new HashSet<>();

    private void addEdgesOfCell(Board b,Point p) {
        for (Direction d : Direction.orthogonals()) {
            if (b.getEdge(p.x,p.y,d) != EdgeState.UNKNOWN) continue;
            EdgeContainer.EdgeCoord ec = EdgeContainer.getEdgeCoord(p.x,p.y,d);
            if (edgesseen.contains(ec)) continue;
            edgeorder.add(ec);
            edgesseen.add(ec);
        }
    }


    public EdgeOrdering(Board b) {
        List<PathInfo> leftovers = new ArrayList<>();
        for (PathInfo pi : b.getPathInfo()) {
            if (pi.end1Terminated && pi.end2Terminated) continue;
            if (!pi.end1Terminated && !pi.end2Terminated) leftovers.add(pi);
            if (pi.end1Terminated) {
                addEdgesOfCell(b,pi.p.endTwo());
            }
            if (pi.end2Terminated) {
                addEdgesOfCell(b,pi.p.endOne());
            }
        }
        for (PathInfo pi : leftovers) {
            addEdgesOfCell(b,pi.p.endOne());
            addEdgesOfCell(b,pi.p.endTwo());
        }

        b.forEachCell((x,y)->{
            if (!b.hasClue(x,y)) return;
            addEdgesOfCell(b,new Point(x,y));
        });

        b.forEachEdge((x,y,isV,old)->{
            if (b.getEdge(x,y,isV) != EdgeState.UNKNOWN) return;
            EdgeContainer.EdgeCoord ec = new EdgeContainer.EdgeCoord(x,y,isV);
            if (edgesseen.contains(ec)) return;
            edgeorder.add(ec);
        });
    }

    @Override public Iterator<EdgeContainer.EdgeCoord> iterator() { return edgeorder.iterator(); }
}
