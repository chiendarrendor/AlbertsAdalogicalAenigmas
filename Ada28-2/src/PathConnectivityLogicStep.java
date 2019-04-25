import grid.graph.PossibleConnectivityDetector;
import grid.logic.LogicStatus;

import java.awt.Point;

public class PathConnectivityLogicStep implements grid.logic.LogicStep<Board> {
    private static class MyReference implements PossibleConnectivityDetector.PossibleConnectivityReference {
        Board b;
        public MyReference(Board b) { this.b = b; }
        @Override public int getWidth() { return b.getWidth(); }
        @Override public int getHeight() { return b.getHeight(); }
        @Override public boolean isConnectedCell(int x, int y) { return b.getCell(x,y) == CellType.PATH; }
        @Override public boolean isPossibleCell(int x, int y) { return b.getCell(x,y) == CellType.UNKNOWN; }
        @Override public boolean edgeExitsEast(int x, int y) { return true; }
        @Override public boolean edgeExitsSouth(int x, int y) { return true; }
    }


    @Override public LogicStatus apply(Board thing) {
        PossibleConnectivityDetector pcon = new PossibleConnectivityDetector(new MyReference(thing));
        if (pcon.isEmpty()) return LogicStatus.STYMIED;
        if (!pcon.isConnected()) return LogicStatus.CONTRADICTION;
        if (pcon.getNonConnectingPossibles().size() > 0) return LogicStatus.CONTRADICTION;

        LogicStatus result = LogicStatus.STYMIED;

        for (Point p : pcon.getArticulatingPossibles()) {
            thing.setCell(p.x,p.y,CellType.PATH);
            result = LogicStatus.LOGICED;
        }

        return result;
    }
}
