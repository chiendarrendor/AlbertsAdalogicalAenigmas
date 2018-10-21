import grid.graph.PossibleConnectivityDetector;
import grid.logic.LogicStatus;
import grid.puzzlebits.Direction;

import java.awt.Point;

public class DotConnectivityLogicStep implements grid.logic.LogicStep<Board> {
    private class MyRef implements PossibleConnectivityDetector.PossibleConnectivityReference {
        private Board b;
        public MyRef(Board b) {this.b = b; }

        public int getWidth() { return b.getWidth(); }
        public int getHeight() { return b.getHeight(); }
        public boolean isConnectedCell(int x, int y) {
            if (b.isDot(x,y)) return true;
            for (Direction d : Direction.orthogonals()) {
                if (b.getEdge(x,y,d) == EdgeState.PATH) return true;
            }
            return false;
        }

        @Override public boolean isPossibleCell(int x, int y) {
            return !isConnectedCell(x,y);
        }

        @Override public boolean edgeExitsEast(int x, int y) {
            return b.getEdge(x,y,Direction.EAST) != EdgeState.WALL;
        }

        @Override public boolean edgeExitsSouth(int x, int y) {
            return b.getEdge(x,y,Direction.SOUTH) != EdgeState.WALL;
        }
    }
    @Override public LogicStatus apply(Board thing) {
        PossibleConnectivityDetector pcd = new PossibleConnectivityDetector(new MyRef(thing));
        if (pcd.isEmpty()) throw new RuntimeException("How did this happen?");
        if (!pcd.isConnected()) return LogicStatus.CONTRADICTION;

        LogicStatus result = LogicStatus.STYMIED;

        for (Point p : pcd.getNonConnectingPossibles()) {
            for (Direction d : Direction.orthogonals()) {
                if (thing.getEdge(p.x,p.y,d) == EdgeState.UNKNOWN) {
                    result = LogicStatus.LOGICED;
                    thing.setEdge(p.x,p.y,d,EdgeState.WALL);
                }
            }
        }

        for (Point p: pcd.getArticulatingPossibles()) {
            if (!thing.articulates(p.x,p.y)) {
                result = LogicStatus.LOGICED;
                thing.setArticulates(p.x,p.y);
            }
        }

        return result;
    }
}
