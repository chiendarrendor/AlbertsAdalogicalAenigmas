import grid.logic.LogicStatus;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.traverse.DepthFirstIterator;

import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NoLoopsLogicStep implements grid.logic.LogicStep<Board> {

    private boolean isCyclicUtil(Graph<Point,DefaultEdge> graph, Point curvertex, Set<Point> visited, Point parent) {
        visited.add(curvertex);

        for (Point p : Graphs.neighborListOf(graph,curvertex)) {
            if (!visited.contains(p)) {
                if (isCyclicUtil(graph,p,visited,curvertex)) return true;
            }
            else if (!p.equals(parent)) return true;
        }
        return false;
    }

    private boolean isCycle(Graph<Point,DefaultEdge> graph) {
        Set<Point> visited = new HashSet<>();
        for (Point p : graph.vertexSet()) {
            if (visited.contains(p)) continue;
            if (isCyclicUtil(graph,p,visited,null)) return true;
        }
        return false;
    }


    @Override public LogicStatus apply(Board thing) {

        for(int postid : thing.getPostIds()) {
            Board.PostCountState pcs = thing.getPostCountState(postid);
            if (pcs.getWallCount() > 2) return LogicStatus.CONTRADICTION;
        }

        SimpleGraph<Point,DefaultEdge> sg = new SimpleGraph<Point,DefaultEdge>(DefaultEdge.class);
        for (int postid : thing.getPostIds()) {
            sg.addVertex(thing.getPost(postid).location);
        }
        for (int fenceid : thing.getFenceIds()) {
            if (thing.getFenceState(fenceid) != EdgeType.WALL) continue;
            Fence f = thing.getFence(fenceid);
            if (f.p2 == null) continue;
            sg.addEdge(f.p1.location,f.p2.location);
        }

        if (isCycle(sg)) return LogicStatus.CONTRADICTION;

        return LogicStatus.STYMIED;
    }
}
