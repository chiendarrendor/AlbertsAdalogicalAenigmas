import grid.graph.GridGraph;
import grid.logic.LogicStatus;

import java.awt.Point;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NoLoopPathLogicStep implements grid.logic.LogicStep<Board> {
    public static class LoopRef implements GridGraph.GridReference {
        Board b;
        public LoopRef(Board b) { this.b = b; }
        public int getWidth() { return b.getWidth(); }
        public int getHeight() { return b.getHeight(); }
        public boolean isIncludedCell(int x,int y) { return b.getCell(x,y) == CellType.PATH; }
        public boolean edgeExitsEast(int x,int y) { return true; }
        public boolean edgeExitsSouth(int x,int y) { return true; }
    }

    public LogicStatus apply(Board thing) {
        GridGraph gg = new GridGraph(new LoopRef(thing));
        if (gg.hasCycle()) return LogicStatus.CONTRADICTION;


        List<Point> path = gg.shortestPathBetween(thing.getStart(),thing.getEnd());
        if (path == null) return LogicStatus.STYMIED;

        for (Point pathpoint : path) {
            if (thing.getShape(pathpoint.x,pathpoint.y) == CellShape.TRIANGLE) return LogicStatus.CONTRADICTION;
        }

        Set<Point> pathset = new HashSet<>();
        pathset.addAll(path);

        for (Point circlepoint : thing.getCircleSet()) {
            if (!pathset.contains(circlepoint)) return LogicStatus.CONTRADICTION;
        }

        return LogicStatus.STYMIED;
    }
}
