import grid.graph.PossibleConnectivityDetector;
import grid.logic.LogicStatus;

import java.awt.Point;
import java.util.Collection;

public class ConnectedShadedCellsLogicStep implements grid.logic.LogicStep<Board> {
    private static class MyReference implements PossibleConnectivityDetector.PossibleConnectivityReference {
        Board b;
        public MyReference(Board b) { this.b = b; }
        @Override public int getWidth() { return b.getWidth(); }
        @Override public int getHeight() { return b.getHeight(); }
        @Override public boolean isConnectedCell(int x, int y) { return b.getCell(x,y) == CellState.SHADED; }
        @Override public boolean isPossibleCell(int x, int y) { return b.getCell(x,y) == CellState.UNKNOWN; }
        @Override public boolean edgeExitsEast(int x, int y) { return true; }
        @Override public boolean edgeExitsSouth(int x, int y) { return true; }
    }


    @Override public LogicStatus apply(Board thing) {
        PossibleConnectivityDetector pcd = new PossibleConnectivityDetector(new MyReference(thing));
        if (pcd.isEmpty()) return LogicStatus.STYMIED;
        if (!pcd.isConnected()) return LogicStatus.CONTRADICTION;

        Collection<Point> nonConnectingPossibles = pcd.getNonConnectingPossibles();
        Collection<Point> articulatingPossibles = pcd.getArticulatingPossibles();

        nonConnectingPossibles.stream().forEach(p->thing.setCell(p.x,p.y,CellState.UNSHADED));
        articulatingPossibles.stream().forEach(p->thing.setCell(p.x,p.y,CellState.SHADED));
        return (nonConnectingPossibles.size() > 0 || articulatingPossibles.size() > 0) ? LogicStatus.LOGICED : LogicStatus.STYMIED;
    }
}
