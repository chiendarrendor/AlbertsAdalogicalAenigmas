import grid.graph.GridGraph;
import grid.logic.LogicStatus;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.Set;

public class NoEmptyPenLogicStep implements grid.logic.LogicStep<Board> {
    private static class MyReference implements GridGraph.GridReference {
        Board b;
        public MyReference(Board b) { this.b = b; }
        @Override public int getWidth() { return b.getWidth(); }
        @Override public int getHeight() { return b.getHeight(); }
        @Override public boolean isIncludedCell(int x, int y) { return true; }

        @Override public boolean edgeExitsEast(int x, int y) { return b.getEdge(x,y, Direction.EAST) != EdgeType.WALL; }
        @Override public boolean edgeExitsSouth(int x, int y) { return b.getEdge(x,y,Direction.SOUTH) != EdgeType.WALL; }
    }

    private boolean hasAnimal(Board thing,Set<Point> conset) {
        for (Point p : conset) if (thing.hasAnimal(p.x,p.y)) return true;
        return false;
    }

    @Override public LogicStatus apply(Board thing) {
        GridGraph gg = new GridGraph(new MyReference(thing));
        for (Set<Point> conset : gg.connectedSets()) {
            if (!hasAnimal(thing,conset)) return LogicStatus.CONTRADICTION;
        }

        return LogicStatus.STYMIED;
    }
}
