import grid.graph.GridGraph;
import grid.logic.LogicStatus;

import java.awt.Point;
import java.util.Set;

public class NoEmptyWhiteRegionLogicStep implements grid.logic.LogicStep<Board> {
    public static class WhiteGroupGridReference implements GridGraph.GridReference {
        Board b;
        public WhiteGroupGridReference(Board b) { this.b = b; }
        @Override public int getWidth() { return b.getWidth(); }
        @Override public int getHeight() { return b.getHeight(); }
        @Override public boolean isIncludedCell(int x, int y) { return b.getCell(x,y) != CellType.BLACK; }
        @Override public boolean edgeExitsEast(int x, int y) { return true; }
        @Override public boolean edgeExitsSouth(int x, int y) { return true; }
    }



    @Override public LogicStatus apply(Board thing) {
        GridGraph gg = new GridGraph(new WhiteGroupGridReference(thing));

        for (Set<Point> conset : gg.connectedSets()) {
            if (conset.stream().allMatch(p->thing.getRegion(p.x,p.y) == null)) return LogicStatus.CONTRADICTION;
        }

        return LogicStatus.STYMIED;
    }
}
