import grid.graph.PossibleConnectivityDetector;
import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.*;

/**
 * Created by chien on 12/16/2017.
 */
public class ConnectivityLogicStep implements LogicStep<Board>
{
    private class MyDetectorReference implements PossibleConnectivityDetector.PossibleConnectivityReference
    {
        Board thing;
        public MyDetectorReference(Board thing) { this.thing = thing; }
        public int getWidth() { return thing.getWidth(); }
        public int getHeight() { return thing.getHeight(); }
        public boolean isConnectedCell(int x, int y) { return thing.ob.getCellColor(x,y) == CellColor.WHITE; }
        public boolean isPossibleCell(int x, int y) { return thing.ob.getCellColor(x,y) == CellColor.UNKNOWN; }
        public boolean edgeExitsEast(int x, int y) { return true; }
        public boolean edgeExitsSouth(int x, int y) { return true; }
    }

    public LogicStatus apply(Board thing)
    {
        PossibleConnectivityDetector pcd = new PossibleConnectivityDetector(new MyDetectorReference(thing));
        if (pcd.isEmpty()) return LogicStatus.STYMIED;
        if (!pcd.isConnected()) return LogicStatus.CONTRADICTION;

        LogicStatus result = LogicStatus.STYMIED;

        for (Point p : pcd.getNonConnectingPossibles()) { result = LogicStatus.LOGICED ; thing.ob.setCellColor(p.x,p.y,CellColor.BLACK); }
        for (Point p : pcd.getArticulatingPossibles()) { result = LogicStatus.LOGICED; thing.ob.setCellColor(p.x,p.y,CellColor.WHITE); }
        return result;
    }
}
