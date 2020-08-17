import grid.graph.PossibleConnectivityDetector;
import grid.logic.LogicStatus;

import java.awt.Point;

public class SingleGroupOutsideLogicStep implements grid.logic.LogicStep<Board> {
    // algorithm:  all outside cells must connect to the edge of the board.
    // to have that function as a single connectivity, we are going to make the
    // calculated board one step larger in every direction, and have outside cells all around the
    // extended outer edge.
    private static class MyReference implements PossibleConnectivityDetector.PossibleConnectivityReference {
        Board b;
        int right;
        int bottom;
        public MyReference(Board b) { this.b = b; right = b.getWidth() + 1; bottom = b.getHeight() + 1; }
        @Override public int getWidth() { return b.getWidth() + 2 ; }
        @Override public int getHeight() { return b.getHeight() + 2; }
        @Override public boolean isConnectedCell(int x, int y) {
            if (x == 0 || x == right || y == 0 || y == bottom) return true;
            return b.getCell(x-1,y-1) == CellType.OUTSIDE;
        }

        @Override public boolean isPossibleCell(int x, int y) {
            if (x == 0 || x == right || y == 0 || y == bottom) return false;
            return b.getCell(x-1,y-1) == CellType.UNKNOWN;
        }
        @Override public boolean edgeExitsEast(int x, int y) { return true; }
        @Override public boolean edgeExitsSouth(int x, int y) { return true; }
    }

    @Override public LogicStatus apply(Board thing) {
        PossibleConnectivityDetector pcd = new PossibleConnectivityDetector(new MyReference(thing));
        if (pcd.isEmpty()) return LogicStatus.STYMIED;
        if (!pcd.isConnected()) return LogicStatus.CONTRADICTION;

        LogicStatus result = LogicStatus.STYMIED;
        for (Point p : pcd.getNonConnectingPossibles()) {
            thing.setCell(p.x-1,p.y-1,CellType.INSIDE);
            result = LogicStatus.LOGICED;
        }

        for (Point p : pcd.getArticulatingPossibles()) {
            thing.setCell(p.x-1,p.y-1,CellType.OUTSIDE);
            result = LogicStatus.LOGICED;
        }


        return result;
    }
}
