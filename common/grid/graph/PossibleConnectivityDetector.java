package grid.graph;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by chien on 12/13/2017.
 *
 * Given a grid of cells that requires some of them
 * of a particular type to be connected, and acknowledges that some other cells
 * are in a state where they might be connectors or not, (as well as the possibility that
 * not all edges between cells are traversable) this class will determine
 * four things:
 * 0) are there any must-be-connected cells at all?  (if no, then the last three items are nonsensical)
 * 1) are all known must-be-connected cells actually connectable? (if no, then the last two items are nonsensical)
 * 2) what is the list of possible-connected cells that are not connectable to any must-be-connected cells?
 * 3) are there any possible-connected cells that, if removed, would break the must-be-connected cells up?
 */
public class PossibleConnectivityDetector
{
    public interface PossibleConnectivityReference
    {
        int getWidth();
        int getHeight();
        boolean isConnectedCell(int x,int y);
        boolean isPossibleCell(int x,int y);
        boolean edgeExitsEast(int x, int y);
        boolean edgeExitsSouth(int x,int y);
    }


    private Set<Point> limitSet = null;
    private Point limitPoint = null;

    private class MyGridReference implements GridGraph.GridReference
    {
        private PossibleConnectivityReference pcr;
        public MyGridReference(PossibleConnectivityReference pcr) { this.pcr = pcr; }
        public int getWidth() { return pcr.getWidth(); }
        public int getHeight() { return pcr.getHeight(); }
        public boolean isIncludedCell(int x,int y)
        {
            Point p = new Point(x,y);
            if (limitSet != null && !limitSet.contains(p)) return false;
            if (limitPoint != null && limitPoint.equals(p)) return false;

            return pcr.isConnectedCell(x,y) || pcr.isPossibleCell(x,y);
        }
        public boolean edgeExitsEast(int x,int y) { return pcr.edgeExitsEast(x,y); }
        public boolean edgeExitsSouth(int x,int y) { return pcr.edgeExitsSouth(x,y); }
    }

    private class ConnectedProcessor
    {
        Vector<Set<Point>> cons = new Vector<>();
        Vector<Set<Point>> noncons = new Vector<>();

        public ConnectedProcessor(List<Set<Point>> consets,PossibleConnectivityReference pcr)
        {
            for (Set<Point> sp : consets)
            {
                boolean found = false;
                for (Point p : sp)
                {
                    if (pcr.isConnectedCell(p.x, p.y))
                    {
                        found = true;
                        break;
                    }
                }
                if (found) cons.add(sp);
                else noncons.add(sp);
            }
        }
    }

    private boolean empty;
    private boolean connected;
    private Vector<Point> nonconPossibles = new Vector<>();
    private Vector<Point> articulatingPossibles = new Vector<>();
    public boolean isEmpty() { return empty; }
    public boolean isConnected() { return connected; }
    public Vector<Point> getNonConnectingPossibles() { return nonconPossibles; }
    public Vector<Point> getArticulatingPossibles() { return articulatingPossibles; }


    public PossibleConnectivityDetector(PossibleConnectivityReference pcr)
    {
        MyGridReference mgr = new MyGridReference(pcr);
        GridGraph firstgg = new GridGraph(mgr);
        ConnectedProcessor cp1 = new ConnectedProcessor(firstgg.connectedSets(),pcr);

        // special case to note...if there are no connected cells at all...
        empty = cp1.cons.size() == 0;
        if (empty) return;

        connected = cp1.cons.size() == 1;
        if (!connected) return;
        for(Set<Point> sp : cp1.noncons) nonconPossibles.addAll(sp);

        // if we are here, we have cells to be connected, all in one connected set.
        // we must get the set of articulation points of this connected set.
        limitSet = cp1.cons.elementAt(0);
        GridGraph apgg = new GridGraph(mgr);
        Set<Point> arts = apgg.getArticulationPoints();

        for (Point p : arts)
        {
            if (!pcr.isPossibleCell(p.x,p.y)) continue;
            limitPoint = p;
            GridGraph lpgg = new GridGraph(mgr);
            ConnectedProcessor cplp = new ConnectedProcessor(lpgg.connectedSets(),pcr);
            if (cplp.cons.size() > 1) articulatingPossibles.add(p);
        }
    }

}
