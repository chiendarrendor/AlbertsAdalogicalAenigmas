import grid.graph.PossibleConnectivityDetector;
import grid.logic.LogicStatus;

import java.awt.Point;

public class LabelConnectivityLogicStep implements grid.logic.LogicStep<Board> {

    private class MyReference implements PossibleConnectivityDetector.PossibleConnectivityReference {
        Board thing;
        public MyReference(Board thing) { this.thing = thing; }
        @Override public int getWidth() { return thing.getWidth(); }
        @Override public int getHeight() { return thing.getHeight(); }
        @Override public boolean edgeExitsEast(int x, int y) { return true; }
        @Override public boolean edgeExitsSouth(int x, int y) { return true; }
        @Override public boolean isConnectedCell(int x, int y) { return thing.getCell(x,y) == CellState.LABEL; }
        @Override public boolean isPossibleCell(int x, int y) { return thing.getCell(x,y) == CellState.UNKNOWN; }
    }


    @Override public LogicStatus apply(Board thing) {
        PossibleConnectivityDetector pcd = new PossibleConnectivityDetector(new MyReference(thing));

        if (pcd.isEmpty()) throw new RuntimeException("Board should never be empty!");
        if (!pcd.isConnected()) return LogicStatus.CONTRADICTION;

        LogicStatus result = LogicStatus.STYMIED;

        for (Point p : pcd.getNonConnectingPossibles()) {
            thing.setCell(p.x,p.y,CellState.BARRIER);
            result = LogicStatus.LOGICED;
        }

        for (Point p : pcd.getArticulatingPossibles()) {
            thing.setCell(p.x,p.y,CellState.LABEL);
            result = LogicStatus.LOGICED;
        }

        return result;
    }


}
