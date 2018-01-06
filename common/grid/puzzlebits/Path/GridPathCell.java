package grid.puzzlebits.Path;

import java.awt.*;
import java.util.List;
import java.util.Vector;

public class GridPathCell
{
    private Point myloc = new Point();
    private GridPathContainer gpc;
    private Vector<Path> terminalPaths = new Vector<Path>();
    private Vector<Path> internalPaths = new Vector<>();

    public GridPathCell(int x,int y,GridPathContainer gpc) { this.gpc = gpc; myloc.x = x; myloc.y = y; }

    public List<Path> getTerminalPaths() { return terminalPaths; }
    public List<Path> getInternalPaths() { return internalPaths; }

    public void addTerminalPath(Path p) { terminalPaths.add(p); }
    public void addInternalPath(Path p) { internalPaths.add(p); }

    public void removeTerminalPath(Path p) { terminalPaths.remove(p); }
    public void removeInternalPath(Path p) { internalPaths.remove(p); }

    public boolean hasTerminalPath(Path p) { return terminalPaths.contains(p); }
    public boolean hasInternalPath(Path p) { return internalPaths.contains(p); }


    public void closeLoop(Path p)
    {
        removeTerminalPath(p);
        removeTerminalPath(p);
        addInternalPath(p);
        p.Merge(p,myloc);
    }

    public void merge(Path p1, Path p2)
    {
        // this will remove p2 from all GridPathCells (including this one)
        // and the master list of paths
        gpc.removePath(p2);
        gpc.removePath(p1);
        p1.Merge(p2,myloc);
        // this will add the extended p1 to all gridpath cells it covers (internal or terminal as appropriate)
        // and to the master list of paths
        gpc.addPath(p1);


    }



}
