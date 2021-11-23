import grid.puzzlebits.EdgeContainer;
import grid.solverrecipes.singleloopflatten.EdgeState;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.Graph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.SimpleGraph;

import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// classifies the edges in internalEdges vis-a-vis the current state of the board thus:
// I) all cells connected by a chain of PATH internal edges belong to the same RegionPath
//    a) no cell may have more than 2 PATH edges (contradiction if found)
//    b) any cell that has two PATH internal edges is considered internal
//    c) any cell that has only one PATH internal edge is considered an 'end' of the path
//       1) if an end has another PATH edge that is not internal, it is CLOSED
//       2) if an end has only unknown edges that are internal, it is INTERNAL
//       3) otherwise it is POTENTIAL
//
//

public class RegionParser {
    Map<Point,RegionPath> pathReferences = new HashMap<>();
    Set<RegionPath> regionPaths = new HashSet<>();
    Set<EdgeContainer.EdgeCoord> closingEdges = new HashSet<>();
    boolean valid = true;
    int maxconn = 0;
    int lplength = 0;

    public boolean isValid() { return valid; }
    public int getMaxiumumPathLength() { return maxconn; }
    public int getMinimumMaximumPathLength() { return lplength; }

    public void assign(EdgeContainer.EdgeCoord ec,boolean isPath) {
        List<Point> ends = ec.getAdjacentCells();
        RegionPath rp0 = pathReferences.get(ends.get(0));
        RegionPath rp1 = pathReferences.get(ends.get(1));

        if (rp0 == null && rp1 == null) {
            RegionPath newRP = new RegionPath(ec,isPath);
            regionPaths.add(newRP);
            pathReferences.put(ends.get(0),newRP);
            pathReferences.put(ends.get(1),newRP);
        } else if (rp0 == null && rp1 != null) {
            pathReferences.put(ends.get(0),rp1);
            rp1.addEdge(ec);
        } else if (rp1 == null && rp0 != null) {
            pathReferences.put(ends.get(1),rp0);
            rp0.addEdge(ec);
        } else if (rp0 == rp1) {
            if (isPath) {
                valid = false;
                return;
            } else {
                rp0.addEdge(ec);
            }
        } else {
            rp0.addAllEdges(rp1);
            rp1.pointSet().stream().forEach(p->pathReferences.put(p,rp0));
            rp0.addEdge(ec);
            regionPaths.remove(rp1);
        }
    }


    public RegionParser(Board b, Set<EdgeContainer.EdgeCoord> internalEdges, Set<Point> cells) {
        // process all PATH internal edges first, to create the set of RegionPaths
        for (EdgeContainer.EdgeCoord ec : internalEdges) {
            if (b.getEdge(ec.x, ec.y, ec.isV) != EdgeState.PATH) continue;
            assign(ec,true );
            if (!valid) return;
        }

        // a badness is if any of the RegionPaths discover more than two edges to a cell:
        for (RegionPath rp : regionPaths) {
            if (!rp.isValid()) {
                valid = false;
                return;
            }
        }

        // if we get here, then all internal PATH edges have been processed, and no intermal loops or illegal pathing has been discovered.
        // now process UNKNOWN edges (we don't have to process WALL edges since they by definintion don't connect anything)
        // there are three kinds of UNKNOWN edges:
        // 1) both sides are the same RegionPath -- this will create a loop and should be remembered as a 'closing edge'
        //    (closing edges can be marked as WALLS by the calling RegionLogicStep
        // 2) one or both sides are RegionPath -- this is a 'connecting edge' and will be a vertex in the Connection graph we create below
        // 3) neither side is a RegionPath -- both sides belong to the same RegionBlock
        Set<EdgeContainer.EdgeCoord> linkingEdges = new HashSet<>();

        for (EdgeContainer.EdgeCoord ec : internalEdges) {
            if (b.getEdge(ec.x,ec.y,ec.isV) != EdgeState.UNKNOWN) continue;

            List<Point> ends = ec.getAdjacentCells();
            RegionPath rp0 = pathReferences.get(ends.get(0));
            RegionPath rp1 = pathReferences.get(ends.get(1));

            // cases:
            // rp0:     null    path    block
            // rp1:
            // null     NB      L       AB
            // path     L       L*      L
            // block    AB      L       MB

            // find all the cases where either or both ends is an isPath RegionPath
            if ((rp0 != null && rp0.isPath()) || (rp1 != null && rp1.isPath())) {
                // *special case... if they're both paths, and they're the _same_ path, this this edge, if
                // it is a PATH, would cause a loop (we're assuming here that a loop entirely within one region
                // is invalid...the only way that would happen is if the entire board was a single region...not likely)
                if (rp0 == rp1) {
                    closingEdges.add(ec);
                } else {
                    linkingEdges.add(ec);
                }
            } else {
                // if the unknown edge doesn't touch a path, it must be an internal edge of a block
                // due to structural similarities, we're using RegionPaths to store blocks, and just not validating
                // the number of paths into a cell
                assign(ec,false);
            }
        }

        // Blocks are never invalid, and the above process does not modify any Paths, do no validity check needed here.
        // now that we're here, we have all the paths and blocks, and a list of edges that join them together.

        // cells with all internal edges are either LINKING or WALLS will not have been processed here...create special
        // blocks of size one to handle this case.
        for (Point p : cells) {
            if (!pathReferences.containsKey(p)) {
                RegionPath rp = new RegionPath(p);
                regionPaths.add(rp);
                pathReferences.put(p,rp);
            }
        }

        // first check:
        // make a graph with RegionPaths as vertices and the linkingEdges as edges.
        Graph<RegionPath,DefaultEdge> connections = new SimpleGraph<RegionPath,DefaultEdge>(DefaultEdge.class);
        for (RegionPath rp : regionPaths) connections.addVertex(rp);
        for (EdgeContainer.EdgeCoord ec : linkingEdges) {
            List<Point> ends = ec.getAdjacentCells();
            connections.addEdge(pathReferences.get(ends.get(0)),pathReferences.get(ends.get(1)));
        }
        // for each connected set of this graph, sum the sizes of each connected set.   if none of them are at least
        // required size, pathing this region will be impossible.
        ConnectivityInspector<RegionPath,DefaultEdge> connectivity = new ConnectivityInspector<RegionPath,DefaultEdge>(connections);
        maxconn = 0;
        for (Set<RegionPath> conlist : connectivity.connectedSets()) {
            int size = conlist.stream().mapToInt(rp->rp.size()).sum();
            if (size > maxconn) maxconn = size;
        }

        // last step:   get a list of all the Paths that are longest.  if the longest path is longer than the region required
        // size,  this will be problematic....note that we use the extended length (for each end, if there are no
        // external edges, we have to count at least one more)
        lplength =  0;

        for (RegionPath rp : regionPaths) {
            if (!rp.isPath()) continue;
            rp.calculateTerminals(b,internalEdges);
            int rplen = rp.getExtendedSize();
            if (rplen > lplength) lplength = rplen;
        }
    }
}
