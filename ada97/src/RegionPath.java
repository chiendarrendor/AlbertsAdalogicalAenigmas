import grid.puzzlebits.Direction;
import grid.puzzlebits.EdgeContainer;
import grid.solverrecipes.singleloopflatten.EdgeState;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RegionPath {
    // called only on cells that are known to be terminals of this path (i.e. they have exactly one PATH edge in internalEdges
    public class Terminal {
        Point p;
        Direction internalTo = null;
        List<Direction> internalUnknown = new ArrayList<>();
        Direction externalPath = null;
        List<Direction> externalUnknown = new ArrayList<>();
        // this is the amount by which this terminal extends the size of the path
        public int size() {
            if (externalPath == null && externalUnknown.size() == 0) return 1;
            return 0;
        }

        public Terminal(Point p, Board b, Set<EdgeContainer.EdgeCoord> internalEdges) {
            this.p = p;
            for (Direction d : Direction.orthogonals()) {
                EdgeContainer.EdgeCoord ec = EdgeContainer.getEdgeCoord(p.x,p.y,d);
                switch(b.getEdge(p.x,p.y,d)) {
                    case UNKNOWN:
                        if (internalEdges.contains(ec)) {
                            internalUnknown.add(d);
                        } else {
                            externalUnknown.add(d);
                        }
                        break;
                    case PATH:
                        if (internalEdges.contains(ec)) {
                            internalTo = d;
                        } else {
                            externalPath = d;
                        }
                        break;
                    case WALL: break;
                }
            }
        }
    }

    Terminal t1 = null;
    Terminal t2 = null;
    Set<EdgeContainer.EdgeCoord> edges = new HashSet<>();
    Map<Point,Integer> pathCount = new HashMap<>();
    Set<Point> cells = new HashSet<>();
    boolean valid = true;
    boolean isPath;  // this is true if this is a path, false if this is simply a block.
    // difference is whether we assume that the cells are connected only via PATH Edges

    private void incrementPoint(Point p) {
        if (!pathCount.containsKey(p)) pathCount.put(p,0);
        pathCount.put(p, pathCount.get(p)+1);
        if (pathCount.get(p) > 2) valid = false;
    }

    public RegionPath(EdgeContainer.EdgeCoord ec,boolean isPath) {
        this.isPath = isPath;
        addEdge(ec);
    }

    // special constructor for size-one blocks.
    public RegionPath(Point p) {
        isPath = false;
        cells.add(p);
    }


    public void addEdge(EdgeContainer.EdgeCoord ec) {
        List<Point> ends = ec.getAdjacentCells();
        cells.addAll(ends);
        edges.add(ec);
        if (isPath) {
            incrementPoint(ends.get(0));
            incrementPoint(ends.get(1));
        }

    }

    public void addAllEdges(RegionPath other) {
        other.edges.stream().forEach(ec->addEdge(ec));
    }

    public Collection<Point> pointSet() { return cells; }
    public boolean isValid() { return valid; }
    public boolean isPath() { return isPath; }
    public int size() { return cells.size(); }
    public Terminal getTerminal1() { return t1; }
    public Terminal getTerminal2() { return t2; }

    public void calculateTerminals(Board b, Set<EdgeContainer.EdgeCoord> internalEdges) {
        if (!isPath()) throw new RuntimeException("Can't Calculate Terminals on a non-path");
        if (!isValid()) throw new RuntimeException("Can't calculate terminals on an invalid path");
        for (Point p : cells) {
            if (pathCount.get(p) > 1) continue;
            if (t1 == null) t1 = new Terminal(p,b,internalEdges);
            else if (t2 == null) {
                t2 = new Terminal(p,b,internalEdges);
                break;
            }
        }
    }

    public int getExtendedSize() {
        return size() + getTerminal1().size() + getTerminal2().size();
    }


}
