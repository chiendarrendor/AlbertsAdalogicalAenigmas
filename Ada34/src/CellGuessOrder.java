import grid.puzzlebits.Path.GridPathCell;
import grid.puzzlebits.Path.Path;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class CellGuessOrder implements Iterable<Point> {
    List<Point> cells = new ArrayList<>();
    Set<Point> seen = new HashSet<>();

    private void insert(Point p) {
        if (seen.contains(p)) return;
        seen.add(p);
        cells.add(p);
    }

    private Path getPath(Board b, int x,int y) {
        GridPathCell gpc = b.getPathContainer().getCell(x,y);
        if (gpc.getTerminalPaths().size() > 0) return gpc.getTerminalPaths().get(0);
        if (gpc.getInternalPaths().size() > 0) return gpc.getInternalPaths().get(0);
        return null;
    }


    public CellGuessOrder(Board b) {
        b.cleanPaths();


        for (Point p : b.numberclues) {
            insert(p);
            Path path = getPath(b,p.x,p.y);
            if (path != null) {
                for (Point pp : path) {
                    insert(pp);
                }
            }
        }
        b.forEachCell((x,y)->insert(new Point(x,y)));
    }

    @Override public Iterator<Point> iterator() { return cells.iterator(); }
}
