import grid.graph.GridGraph;
import grid.logic.simple.LogicStatus;
import grid.logic.LogicStep;

import java.awt.*;
import java.util.Set;

/**
 * Created by chien on 5/19/2017.
 */
public class ConnectivityLogicStep implements LogicStep<Board>
{
    private class MyGridReference implements GridGraph.GridReference
    {
        Board b;
        public MyGridReference(Board b) { this.b = b; }
        public int getWidth() { return b.getWidth(); }
        public int getHeight() { return b.getHeight(); }
        public boolean isIncludedCell(int x, int y) { return b.getCell(x,y) != CellState.BLACK; }
        public boolean edgeExitsEast(int x, int y) { return true; }
        public boolean edgeExitsSouth(int x, int y) { return true; }
    }


    @Override
    public LogicStatus apply(Board thing)
    {
        GridGraph gg = new GridGraph(new MyGridReference(thing));
        if (!gg.isConnected()) return LogicStatus.CONTRADICTION;
        Set<Point> arts = gg.getArticulationPoints();

        LogicStatus result = LogicStatus.STYMIED;
        for (Point art : arts)
        {
            if (thing.getCell(art.x,art.y) == CellState.WHITE) continue;
            result = LogicStatus.LOGICED;
            thing.setCellWhite(art.x,art.y);
        }

        return result;
    }
}
