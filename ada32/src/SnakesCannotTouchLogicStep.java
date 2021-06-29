import grid.graph.GridGraph;
import grid.logic.LogicStatus;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

public class SnakesCannotTouchLogicStep implements grid.logic.LogicStep<Board> {

    private static class MyReference implements GridGraph.GridReference {
        private Board b;
        public MyReference(Board b) { this.b = b; }

        @Override public int getWidth() { return b.getWidth(); }
        @Override public int getHeight() { return b.getHeight(); }
        @Override public boolean isIncludedCell(int x, int y) {
            NumCell ns = b.getCell(x,y);
            return ns.isDone() && ns.getComplete() != 0;
        }

        @Override public boolean edgeExitsEast(int x, int y) { return true; }
        @Override public boolean edgeExitsSouth(int x, int y) { return true; }
    }


    @Override public LogicStatus apply(Board thing) {
        GridGraph gg = new GridGraph(new MyReference(thing));

        for(Set<Point> pset : gg.connectedSets()) {
            Set<Integer> numseen = new HashSet<>();
            for (Point p : pset) {
                int v = thing.getCell(p.x,p.y).getComplete();
                if (numseen.contains(v)) return LogicStatus.CONTRADICTION;
                numseen.add(v);
            }
        }

        return LogicStatus.STYMIED;
    }
}
