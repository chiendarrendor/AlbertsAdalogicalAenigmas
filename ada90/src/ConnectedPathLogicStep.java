import grid.graph.PossibleConnectivityDetector;
import grid.logic.LogicStatus;

public class ConnectedPathLogicStep implements grid.logic.LogicStep<Board> {
    private static class MyReference implements PossibleConnectivityDetector.PossibleConnectivityReference {
        Board b;
        public MyReference(Board b) { this.b = b; }
        @Override public int getWidth() { return b.getWidth(); }
        @Override public int getHeight() { return b.getHeight(); }
        @Override public boolean isConnectedCell(int x, int y) { return b.getCellState(x,y) == CellState.PATH; }
        @Override public boolean isPossibleCell(int x, int y) { return b.getCellState(x,y) == CellState.UNKNOWN; }
        @Override public boolean edgeExitsEast(int x, int y) { return true; }
        @Override public boolean edgeExitsSouth(int x, int y) { return true; }
    }


    @Override public LogicStatus apply(Board thing) {
        PossibleConnectivityDetector pcd = new PossibleConnectivityDetector(new MyReference(thing));

        if (pcd.isEmpty()) throw new RuntimeException("No clues on board?");
        if (!pcd.isConnected()) return LogicStatus.CONTRADICTION;




        return null;
    }
}
