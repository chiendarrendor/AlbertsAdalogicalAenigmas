import grid.graph.GridGraph;
import grid.logic.LogicStatus;
import grid.puzzlebits.Direction;
import grid.solverrecipes.singleloopflatten.EdgeState;

import java.awt.Point;
import java.util.List;
import java.util.Set;

public class SeparationLogicStep implements grid.logic.LogicStep<Board> {
    private static class MyGridReference implements GridGraph.GridReference {
        private Board b;
        public MyGridReference(Board b) { this.b = b; }
        public int getWidth() { return b.getWidth(); }
        public int getHeight() { return b.getHeight(); }
        public boolean isIncludedCell(int x,int y) { return true; }
        public boolean edgeExitsEast(int x,int y) { return b.getEdge(x,y, Direction.EAST) != EdgeState.WALL; }
        public boolean edgeExitsSouth(int x,int y) { return b.getEdge(x,y,Direction.SOUTH) != EdgeState.WALL; }
    }

    @Override public LogicStatus apply(Board thing) {
        GridGraph gg = new GridGraph(new MyGridReference(thing));
        List<Set<Point>> consets = gg.connectedSets();

        for (Set<Point> conset : consets) {
            int matecount = 0;
            int lovercount = 0;
            for (Point p : conset) {
                if (thing.isLover(p.x,p.y)) ++lovercount;
                if (thing.isMate(p.x,p.y)) ++matecount;
            }

            if (matecount != lovercount) return LogicStatus.CONTRADICTION;
            if (matecount == 0) return LogicStatus.CONTRADICTION;
        }

        return LogicStatus.STYMIED;

    }
}
