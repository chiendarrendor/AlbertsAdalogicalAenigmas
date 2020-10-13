import grid.graph.PossibleConnectivityDetector;
import grid.logic.LogicStatus;

import java.awt.Point;

public class AllCellsConnectedLogicStep implements grid.logic.LogicStep<Board> {
    private class MyReference implements PossibleConnectivityDetector.PossibleConnectivityReference {
        Board b;
        public MyReference(Board thing) { b = thing; }
        @Override public int getWidth() { return b.getWidth(); }
        @Override public int getHeight() { return b.getHeight(); }
        @Override public boolean isConnectedCell(int x, int y) { return b.getCell(x,y) == CellType.PATH; }
        @Override public boolean isPossibleCell(int x, int y) { return b.getCell(x,y) == CellType.UNKNOWN; }
        @Override public boolean edgeExitsEast(int x, int y) { return true; }
        @Override public boolean edgeExitsSouth(int x, int y) { return true; }
    }

    @Override public LogicStatus apply(Board thing) {


        PossibleConnectivityDetector pcd = new PossibleConnectivityDetector(new MyReference(thing));
        if (pcd.isEmpty()) return LogicStatus.CONTRADICTION;
        if (!pcd.isConnected()) return LogicStatus.CONTRADICTION;
        LogicStatus result = LogicStatus.STYMIED;
        for (Point p : pcd.getNonConnectingPossibles()) {
            result = LogicStatus.LOGICED;
            thing.setCell(p.x,p.y,CellType.WALL);
        }
        for (Point p : pcd.getArticulatingPossibles()) {
            result = LogicStatus.LOGICED;
            thing.setCell(p.x,p.y,CellType.PATH);
        }


        return result;
    }


}
