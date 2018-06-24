import grid.graph.GridGraph;
import grid.logic.LogicStatus;
import grid.puzzlebits.Direction;

public class ConnectivityLogicStep implements grid.logic.LogicStep<Board> {

    private class MyReference implements GridGraph.GridReference {
        Board b;
        public MyReference(Board b) { this.b = b; }
        public int getWidth() { return b.getWidth(); }
        public int getHeight() { return b.getHeight(); }
        public boolean isIncludedCell(int x, int y) { return b.getCell(x,y) == CellState.PATH || b.getCell(x,y) == CellState.UNKNOWN; }
        public boolean edgeExitsEast(int x, int y) { return b.getEdge(x,y, Direction.EAST) != EdgeState.WALL; }
        public boolean edgeExitsSouth(int x, int y) { return b.getEdge(x,y,Direction.SOUTH) != EdgeState.WALL; }
    }


    public LogicStatus apply(Board thing) {
        GridGraph gg = new GridGraph(new MyReference(thing));
        if (gg.getArticulationPoints().size() > 0) return LogicStatus.CONTRADICTION;
        return LogicStatus.STYMIED;
    }
}
