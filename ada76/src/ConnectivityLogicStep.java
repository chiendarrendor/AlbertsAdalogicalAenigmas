import grid.graph.PossibleConnectivityDetector;
import grid.logic.LogicStatus;

import java.awt.Point;

public class ConnectivityLogicStep implements grid.logic.LogicStep<Board> {

    private static class BadGridException extends RuntimeException {};

    private static class MyReference implements PossibleConnectivityDetector.PossibleConnectivityReference {
        Board thing;
        public MyReference(Board thing) { this.thing = thing; }
        @Override public int getWidth() { return thing.getWidth(); }
        @Override public int getHeight() { return thing.getHeight();    }
        @Override public boolean edgeExitsEast(int x, int y) { return true; }
        @Override public boolean edgeExitsSouth(int x, int y) { return true; }

        @Override public boolean isConnectedCell(int x, int y) {
            Cell cell = thing.getCell(x,y);
            if (cell.isEmpty()) throw new BadGridException();
            // a cell must be connected if it cannot be blank
            return !cell.hasPossible(CellShape.BLANK);
        }

        @Override public boolean isPossibleCell(int x, int y) {
            Cell cell = thing.getCell(x,y);
            if (cell.isEmpty()) throw new BadGridException();
            // a cell is possible if it could be blank, but it could also be something else
            return cell.hasPossible(CellShape.BLANK) && !cell.isDone();
        }



    }


    @Override public LogicStatus apply(Board thing) {
        try {
            PossibleConnectivityDetector pcd = new PossibleConnectivityDetector(new MyReference(thing));
            if (pcd.isEmpty()) return LogicStatus.STYMIED;
            if (!pcd.isConnected()) return LogicStatus.CONTRADICTION;

            LogicStatus result = LogicStatus.STYMIED;

            for (Point p : pcd.getNonConnectingPossibles()) {
                thing.getCell(p.x,p.y).set(CellShape.BLANK);
                result = LogicStatus.LOGICED;
            }

            for (Point p : pcd.getArticulatingPossibles()) {
                thing.getCell(p.x,p.y).remove(CellShape.BLANK);
                result = LogicStatus.LOGICED;
            }


            return result;

        } catch (BadGridException bge) {
            return LogicStatus.CONTRADICTION;
        }
    }
}
