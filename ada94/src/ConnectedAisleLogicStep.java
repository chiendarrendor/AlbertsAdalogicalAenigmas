import grid.graph.PossibleConnectivityDetector;
import grid.logic.LogicStatus;
import javafx.geometry.Pos;

import java.awt.Point;

public class ConnectedAisleLogicStep implements grid.logic.LogicStep<Board> {
    private static class InvalidCellExeption extends RuntimeException {}


    private static class MyReference implements PossibleConnectivityDetector.PossibleConnectivityReference {
        Board thing;
        public MyReference(Board thing) { this.thing = thing; }
        @Override public int getWidth() { return thing.getWidth(); }
        @Override public int getHeight() { return thing.getHeight(); }
        @Override public boolean isConnectedCell(int x, int y) {
            Cell c = thing.getCell(x,y);
            if (!c.isValid()) throw new InvalidCellExeption();
            return c.isDone() && c.getDoneType() == CellType.AISLE;
        }

        @Override public boolean isPossibleCell(int x, int y) {
            Cell c = thing.getCell(x,y);
            if (!c.isValid()) throw new InvalidCellExeption();
            return !c.isDone() && c.has(CellType.AISLE);
        }

        @Override public boolean edgeExitsEast(int x, int y) { return true; }
        @Override public boolean edgeExitsSouth(int x, int y) { return true; }
    }


    @Override public LogicStatus apply(Board thing) {
        PossibleConnectivityDetector pcd = null;
        try {
            pcd = new PossibleConnectivityDetector(new MyReference(thing));
        } catch(InvalidCellExeption ice) {
            return LogicStatus.CONTRADICTION;
        }

        if (pcd.isEmpty()) return LogicStatus.STYMIED;
        if (!pcd.isConnected()) return LogicStatus.CONTRADICTION;
        LogicStatus result = LogicStatus.STYMIED;
        for(Point p : pcd.getNonConnectingPossibles()) {
            thing.getCell(p.x,p.y).clear(CellType.AISLE);
            result = LogicStatus.LOGICED;
        }
        for (Point p : pcd.getArticulatingPossibles()) {
            thing.getCell(p.x,p.y).set(CellType.AISLE);
            result = LogicStatus.LOGICED;
        }

        return result;
    }
}
