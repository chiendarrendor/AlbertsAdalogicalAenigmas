import java.awt.*;

/**
 * Created by chien on 4/23/2017.
 */
public class ConnectivityLogicStep implements LogicStep<Board>
{
    private class MyGridReference implements GridGraph.GridReference
    {
        private Board board;
        public MyGridReference(Board board) { this.board = board; }

        public int getWidth() { return board.getWidth(); }
        public int getHeight() { return board.getHeight(); }
        public boolean isIncludedCell(int x, int y) { return board.getCell(x,y) != CellType.TREE; }
        public boolean edgeExitsEast(int x, int y) { return true; }
        public boolean edgeExitsSouth(int x, int y) { return true; }
    }


    @Override
    public LogicStatus apply(Board thing)
    {
        GridGraph gg = new GridGraph(new MyGridReference(thing));
        if (!gg.isConnected()) return LogicStatus.CONTRADICTION;

        LogicStatus result = LogicStatus.STYMIED;

        for (Point p : gg.getArticulationPoints())
        {
            if (thing.getCell(p.x,p.y) == CellType.TREE) return LogicStatus.CONTRADICTION;
            if (thing.getCell(p.x,p.y) == CellType.EMPTY) continue;
            thing.setCell(p.x,p.y,CellType.EMPTY);
            result = LogicStatus.LOGICED;
        }
        return result;
    }
}
