import grid.graph.PossibleConnectivityDetector;
import grid.logic.LogicStatus;

import java.awt.Point;
import java.util.List;

public class BlackConnectivityLogicStep implements grid.logic.LogicStep<Board> {
    static class MyReference implements PossibleConnectivityDetector.PossibleConnectivityReference {
        Board b;
        public MyReference(Board b) { this.b = b; }
        @Override public int getWidth() { return b.getWidth(); }
        @Override public int getHeight() { return b.getHeight(); }
        @Override public boolean edgeExitsEast(int x, int y) { return true;  }
        @Override public boolean edgeExitsSouth(int x, int y) { return true;  }
        @Override public boolean isConnectedCell(int x, int y) { return b.getCellType(x,y) == CellType.BLACK; }
        @Override public boolean isPossibleCell(int x, int y) { return b.getCellType(x,y) == CellType.UNKNOWN; }
    }


    @Override public LogicStatus apply(Board thing) {
        PossibleConnectivityDetector pcd = new PossibleConnectivityDetector(new MyReference(thing));
        if (pcd.isEmpty()) return LogicStatus.STYMIED;
        if (!pcd.isConnected()) return LogicStatus.CONTRADICTION;

        LogicStatus result = LogicStatus.STYMIED;

        List<Point> noncons = pcd.getNonConnectingPossibles();
        for (Point p : noncons) {
            thing.setCellType(p.x,p.y,CellType.WHITE);
            result = LogicStatus.LOGICED;
        }

        List<Point> articulations = pcd.getArticulatingPossibles();
        for (Point p : articulations) {
            thing.setCellType(p.x,p.y,CellType.BLACK);
            result = LogicStatus.LOGICED;
        }

        return result;
    }
}
