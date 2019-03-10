import grid.graph.GGFrame;
import grid.graph.GridGraph;
import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.awt.Color;
import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

public class WhiteRegionLogicStep implements LogicStep<Board> {
    int x;
    int y;
    int size;
    Point me;
    public WhiteRegionLogicStep(int x, int y, int size) { this.x = x; this.y = y; this.size = size; me = new Point(x,y); }

    private static class ConnDetectGridReference implements GridGraph.GridReference {
        Board b;
        public ConnDetectGridReference(Board thing) { b = thing; }

        @Override public int getWidth() { return b.getWidth(); }
        @Override public int getHeight() { return b.getHeight(); }
        @Override public boolean isIncludedCell(int x, int y) { return b.getCell(x,y) == CellType.WHITE; }
        @Override public boolean edgeExitsEast(int x, int y) { return true; }
        @Override public boolean edgeExitsSouth(int x, int y) { return true; }
    }



    @Override public LogicStatus apply(Board thing) {
        int myregid = thing.getRegion(x,y);

        GridGraph cdgg = new GridGraph(new ConnDetectGridReference(thing));
        Set<Point> myset = cdgg.connectedSetOf(me);

        if (myset.stream().filter(p->thing.getRegion(p.x,p.y) != null).anyMatch(p->thing.getRegion(p.x,p.y) != myregid)) {
            return LogicStatus.CONTRADICTION;
        }

        LogicStatus result = LogicStatus.STYMIED;
        for (Point p : myset) {
            if (thing.getRegion(p.x,p.y) == null) {
                result = LogicStatus.LOGICED;
                thing.setRegion(p.x,p.y,myregid);
            }
        }


        // if we get here, then myset contains only white cells from this region
        if (myset.size() > size) return LogicStatus.CONTRADICTION;



        Set<Point> adjcells = new HashSet<>();
        for (Point p : myset) {
            for (Direction d : Direction.orthogonals()) {
                Point np = d.delta(p, 1);
                if (!thing.onBoard(np.x, np.y)) continue;
                if (thing.getCell(np.x,np.y) == CellType.BLACK) continue;
                if (myset.contains(np)) continue;
                adjcells.add(np);
            }
        }

        if (myset.size() == size) {
            for (Point p : adjcells) {
                if (thing.getCell(p.x,p.y) == CellType.BLACK) continue;
                if (thing.getCell(p.x,p.y) == CellType.WHITE) return LogicStatus.CONTRADICTION;
                thing.setCell(p.x,p.y,CellType.BLACK);
                result = LogicStatus.LOGICED;
            }
            return result;
        }

        if (adjcells.size() == 0) return LogicStatus.CONTRADICTION;

        if (adjcells.size() == 1) {
            Point p = adjcells.iterator().next();
            thing.setCell(p.x,p.y,CellType.WHITE);
            thing.setRegion(p.x,p.y,myregid);
            return LogicStatus.LOGICED;
        }


        return result;
    }
}
