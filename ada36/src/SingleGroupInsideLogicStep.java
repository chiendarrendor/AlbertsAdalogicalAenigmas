import grid.graph.PossibleConnectivityDetector;
import grid.logic.LogicStatus;

import java.awt.Point;

public class SingleGroupInsideLogicStep implements grid.logic.LogicStep<Board> {
    private static class MyReference implements PossibleConnectivityDetector.PossibleConnectivityReference {
        Board b;
        public MyReference(Board b) { this.b = b; }
        @Override public int getWidth() { return b.getWidth(); }
        @Override public int getHeight() { return b.getHeight(); }
        @Override public boolean isConnectedCell(int x, int y) { return b.getCell(x,y) == CellType.INSIDE; }
        @Override public boolean isPossibleCell(int x, int y) { return b.getCell(x,y) == CellType.UNKNOWN; }
        @Override public boolean edgeExitsEast(int x, int y) { return true; }
        @Override public boolean edgeExitsSouth(int x, int y) { return true; }
    }

    @Override public LogicStatus apply(Board thing) {
        PossibleConnectivityDetector pcd = new PossibleConnectivityDetector(new MyReference(thing));
        if (pcd.isEmpty()) return LogicStatus.STYMIED;
        if (!pcd.isConnected()) return LogicStatus.CONTRADICTION;

        LogicStatus result = LogicStatus.STYMIED;
        for (Point p : pcd.getNonConnectingPossibles()) {
            thing.setCell(p.x,p.y,CellType.OUTSIDE);
            result = LogicStatus.LOGICED;
        }

        for (Point p : pcd.getArticulatingPossibles()) {
            thing.setCell(p.x,p.y,CellType.INSIDE);
            result = LogicStatus.LOGICED;
        }


        return result;
    }
}
