import grid.graph.PossibleConnectivityDetector;
import grid.logic.LogicStatus;

import java.awt.Point;
import java.util.List;

public class PathLogicStep implements grid.logic.LogicStep<Board> {
    private static class MyReference implements PossibleConnectivityDetector.PossibleConnectivityReference {
        Board b;
        public MyReference(Board b) { this.b = b; }

        @Override public int getWidth() { return b.getWidth(); }
        @Override public int getHeight() { return b.getHeight(); }
        @Override public boolean isConnectedCell(int x, int y) {
            CellData cd = b.getCellData(x,y);
            return cd.isPath();
        }

        @Override public boolean isPossibleCell(int x, int y) {
            CellData cd = b.getCellData(x,y);
            return !cd.isComplete() && cd.has(CellData.WALLVALUE);
        }

        @Override public boolean edgeExitsEast(int x, int y) { return true; }
        @Override public boolean edgeExitsSouth(int x, int y) { return true; }
    }


    @Override public LogicStatus apply(Board thing) {
        PossibleConnectivityDetector pcd = new PossibleConnectivityDetector(new MyReference(thing));
        LogicStatus result = LogicStatus.STYMIED;
        if (pcd.isEmpty()) return LogicStatus.STYMIED;
        if (!pcd.isConnected()) return LogicStatus.CONTRADICTION;

        if (pcd.getNonConnectingPossibles().size() > 0) result = LogicStatus.LOGICED;
        pcd.getNonConnectingPossibles().stream().forEach(p->thing.getCellData(p.x,p.y).set(CellData.WALLVALUE));

        if (pcd.getArticulatingPossibles().size() > 0) result = LogicStatus.LOGICED;
        pcd.getArticulatingPossibles().stream().forEach(p->thing.getCellData(p.x,p.y).clear(CellData.WALLVALUE));

        return result;
    }
}
