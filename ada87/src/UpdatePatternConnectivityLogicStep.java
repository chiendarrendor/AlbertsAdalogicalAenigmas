import grid.graph.GridGraph;
import grid.graph.PossibleConnectivityDetector;
import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;
import java.util.List;
import java.util.Set;

public class UpdatePatternConnectivityLogicStep implements LogicStep<Board> {
    char pid;

    public UpdatePatternConnectivityLogicStep(char pid) { this.pid = pid; }



    // every pattern must be a singular connected region
    // this code is operating entirely in pattern space
    @Override public LogicStatus apply(Board thing) {
        LogicStatus result = LogicStatus.STYMIED;
        Pattern p = thing.getPattern(pid);

        /*
        GridGraph gg = new GridGraph(p.getGridReference());
        List<Set<Point>> consets = gg.connectedSets();

        Set<Point> mainset = null;
        Point center = new Point(p.centerx,p.centery);
        for (Set<Point> pset : consets) {
            if (pset.contains(center)) {
                mainset = pset;
                break;
            }
        }
        if (mainset == null) return LogicStatus.CONTRADICTION;
        for(Set<Point> pset : consets) {
            if (pset == mainset) continue;
            for(Point pt : pset) {
                p.setCell(pt.x,pt.y,PatternCell.OUTSIDE);
                result = LogicStatus.LOGICED;
            }
        }
*/



        PossibleConnectivityDetector.PossibleConnectivityReference pcr = p.getConnectivityReference();
        PossibleConnectivityDetector pcd = new PossibleConnectivityDetector(pcr);

        if (pcd.isEmpty()) throw new RuntimeException("Pattern should never be empty!");
        if (!pcd.isConnected()) return LogicStatus.CONTRADICTION;
        List<Point> impossibles = pcd.getNonConnectingPossibles();
        for (Point ip : impossibles) {
            result = LogicStatus.LOGICED;
            p.setCell(ip.x,ip.y,PatternCell.OUTSIDE);
        }
        List<Point> requireds = pcd.getArticulatingPossibles();
        for(Point ip: requireds) {
            result = LogicStatus.LOGICED;
            p.setCell(ip.x,ip.y,PatternCell.INSIDE);
        }



        return result;
    }
}
