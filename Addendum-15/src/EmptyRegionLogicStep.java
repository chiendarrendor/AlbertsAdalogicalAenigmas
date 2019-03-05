import grid.graph.GridGraph;
import grid.logic.LogicStatus;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EmptyRegionLogicStep implements grid.logic.LogicStep<Board> {
    private static class EmptyRegionReference implements GridGraph.GridReference {
        Board b;
        public EmptyRegionReference(Board b) { this.b = b; }

        @Override public int getWidth() { return b.getWidth(); }
        @Override public int getHeight() { return b.getHeight(); }
        @Override public boolean edgeExitsEast(int x, int y) { return true; }
        @Override public boolean edgeExitsSouth(int x, int y) { return true; }
        @Override public boolean isIncludedCell(int x, int y) {
            Region r = b.getRegionByCell(x,y);
            if (r == null) return true;
            return ! r.isDone();
        }


    }



    @Override public LogicStatus apply(Board thing) {
        LogicStatus result = LogicStatus.STYMIED;
        GridGraph emptyBorders = new GridGraph(new EmptyRegionReference(thing));

        List<Set<Point>> empties = new ArrayList<>();

        for (Set<Point> possible : emptyBorders.connectedSets()) {
            if (possible.stream().allMatch(p->thing.cells.getCell(p.x,p.y) == null)) empties.add(possible);
        }

        for (Set<Point> empty : empties) {
            if (empty.stream().allMatch(p -> thing.exclusionZones.getCell(p.x, p.y).contains(empty.size()))) {
                Region r = thing.unNumberedRegion(empty.size());
                empty.stream().forEach(p -> thing.addCell(r.getId(), p));
                result = LogicStatus.LOGICED;
            } else {
                if (empty.size() < 3) return LogicStatus.CONTRADICTION;
                throw new UnhandledEmptyException("Can't handle a split yet! " + empty.size(), thing);
            }
        }


        return result;
    }
}
