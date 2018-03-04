import grid.graph.GridGraph;
import grid.logic.LogicStatus;
import grid.puzzlebits.Direction;
import javafx.geometry.Pos;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

public class RectangleGroupLogicStep implements grid.logic.LogicStep<Board>
{
    private class MyGridReference implements GridGraph.GridReference {
        Board b;
        public MyGridReference(Board b) { this.b = b; }

        public int getWidth() { return b.getWidth(); }
        public int getHeight() { return b.getHeight(); }
        public boolean isIncludedCell(int x, int y) { return b.getCellState(x,y) == CellState.TERMINAL; }
        public boolean edgeExitsEast(int x, int y) { return true; }
        public boolean edgeExitsSouth(int x, int y) { return true; }
    }


    // we want to determine the convex hull of each separate group of
    // TERMINAL circles.  it is a contradiction if each cell in that convex hull
    // is neither TERMINAL nor capable of getting a circle.
    public LogicStatus apply(Board thing)
    {
        GridGraph gg = new GridGraph(new MyGridReference(thing));
        List<Set<Point>> groups = gg.connectedSets();
        LogicStatus result = LogicStatus.STYMIED;

        for (Set<Point> sp : groups) {
            LogicStatus spls = LogicStatus.STYMIED;

            // special case...1 by 1 groups have to be expandable
            if (sp.size() == 1) {
                spls = isSingleExpandable(thing,sp.iterator().next());
            } else
            {
                spls = groupIsBad(thing, sp);
            }
            if (spls == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
            if (spls == LogicStatus.LOGICED) result = LogicStatus.LOGICED;
        }
        return result;
    }

    // if we are here, this single point is a TERMINAL
    // and no adjacent spaces are TERMINAL
    // at least one adjacent space must be INITIAL or EMPTY and have
    // at least one Path that terminates there
    private LogicStatus isSingleExpandable(Board thing, Point theSingle) {
        Set<Path> expanders = new HashSet<>();
        for(Direction d: Direction.orthogonals()) {
            Point np = new Point(theSingle.x + d.DX(),theSingle.y+d.DY());
            if (!thing.onBoard(np)) continue;
            if (thing.getCellState(np.x,np.y) == CellState.PATH) continue;
            PossiblePaths pp = thing.getPossiblePaths(np.x,np.y);
            expanders.addAll(pp.paths);
        }
        if (expanders.size() == 0) return LogicStatus.CONTRADICTION;
        if (expanders.size() > 1) return LogicStatus.STYMIED;

        Path onlyPath = expanders.iterator().next();
        Circle c = thing.circles.get(onlyPath.initial);

        if (!c.placeable(onlyPath)) return LogicStatus.CONTRADICTION;

        c.removeAllPathsBesides(onlyPath);
        c.lock();
        return LogicStatus.LOGICED;
    }

    private class Edges {
        int minx = Integer.MAX_VALUE;
        int miny = Integer.MAX_VALUE;
        int maxx = Integer.MIN_VALUE;
        int maxy = Integer.MIN_VALUE;
        public void addPoint(Point p) {
            if (p.x < minx) minx = p.x;
            if (p.y < miny) miny = p.y;
            if (p.x > maxx) maxx = p.x;
            if (p.y > maxy) maxy = p.y;
        }
        public String toString() { return "[Edges: " + minx + " " + miny + " to " + maxx + " " + maxy + "]" ;}
    }


    private Edges convexHull(Set<Point> sp) {
        Edges result = new Edges();
        sp.stream().forEach((p)->result.addPoint(p));
        return result;
    }



    private LogicStatus groupIsBad(Board thing, Set<Point> sp)
    {
        Set<Path> singletons = new HashSet<>();
        Edges ch = convexHull(sp);

        for (int x = ch.minx ; x <= ch.maxx ; ++x) {
            for (int y = ch.miny ; y <= ch.maxy ; ++y) {
                PossiblePaths pp = thing.getPossiblePaths(x,y);
                if (thing.getCellState(x,y) == CellState.TERMINAL) continue;
                if (thing.getCellState(x,y) == CellState.PATH) return LogicStatus.CONTRADICTION;
                // INITIAL EMPTY
                if (pp.paths.size() == 0) return LogicStatus.CONTRADICTION;
                if (pp.paths.size() > 1) continue;
                singletons.add(pp.paths.iterator().next());
            }
        }

        if (singletons.size() == 0) return LogicStatus.STYMIED;

        for (Path p: singletons) {
            Circle c = thing.circles.get(p.initial);
            if (!c.placeable(p)) return LogicStatus.CONTRADICTION;
            c.removeAllPathsBesides(p);
            c.lock();
        }

        return LogicStatus.LOGICED;
    }
}
