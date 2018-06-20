import grid.graph.PossibleConnectivityDetector;
import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;

public class AllPathConnectedLogicStep implements grid.logic.LogicStep<Board> {
    private static class AllPathReference implements PossibleConnectivityDetector.PossibleConnectivityReference {
        Board b;
        public AllPathReference(Board b) { this.b = b; }
        public int getWidth() { return b.getWidth(); }
        public int getHeight() { return b.getHeight(); }
        public boolean isConnectedCell(int x,int y) { return b.getCell(x,y)==CellType.PATH; }
        public boolean isPossibleCell(int x,int y) { return b.getCell(x,y) == CellType.UNKNOWN; }
        public boolean edgeExitsEast(int x,int y) { return true; }
        public boolean edgeExitsSouth(int x,int y) { return true; }
    }


    public LogicStatus apply(Board thing) {
        PossibleConnectivityDetector pcd = new PossibleConnectivityDetector(new AllPathReference(thing));

        if (pcd.isEmpty()) throw new RuntimeException("How did a board get empty?");
        if (!pcd.isConnected()) return LogicStatus.CONTRADICTION;

        LogicStatus result = LogicStatus.STYMIED;

        for (Point p : pcd.getNonConnectingPossibles()) {
            thing.setCell(p.x,p.y,CellType.WALL);
            result = LogicStatus.LOGICED;
        }

        for (Point p : pcd.getArticulatingPossibles()) {
            thing.setCell(p.x,p.y,CellType.PATH);
            result = LogicStatus.LOGICED;
        }

        return result;
    }
}
