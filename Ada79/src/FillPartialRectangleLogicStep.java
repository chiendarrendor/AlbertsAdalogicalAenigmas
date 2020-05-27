import grid.graph.GridGraph;
import grid.logic.LogicStatus;
import sun.security.provider.SHA;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FillPartialRectangleLogicStep implements grid.logic.LogicStep<Board> {
    public boolean debug = false;


    private static class MyReference implements GridGraph.GridReference {
        Board b;
        public MyReference(Board b) { this.b = b; }
        @Override public int getWidth() { return b.getWidth(); }
        @Override public int getHeight() { return b.getHeight(); }
        @Override public boolean isIncludedCell(int x, int y) { return b.getCell(x,y) == CellState.SHADED; }
        @Override public boolean edgeExitsEast(int x, int y) { return true; }
        @Override public boolean edgeExitsSouth(int x, int y) { return true; }
    }

    public static class Extent {
        int minx = Integer.MAX_VALUE;
        int miny = Integer.MAX_VALUE;
        int maxx = Integer.MIN_VALUE;
        int maxy = Integer.MIN_VALUE;
    }

    public static List<Extent> getExtents(Board thing) {
        List<Extent> result = new ArrayList<Extent>();
        GridGraph gg = new GridGraph(new MyReference(thing));
        List<Set<Point>> consets = gg.connectedSets();

        for (Set<Point> conset : consets) {
            Extent newex = new Extent();
            for (Point p : conset) {
                if (p.x < newex.minx) newex.minx = p.x;
                if (p.y < newex.miny) newex.miny = p.y;
                if (p.x > newex.maxx) newex.maxx = p.x;
                if (p.y > newex.maxy) newex.maxy = p.y;
            }
            result.add(newex);
        }
        return result;
    }




    @Override public LogicStatus apply(Board thing) {
        List<Extent> extents = getExtents(thing);
        LogicStatus result = LogicStatus.STYMIED;

        for (Extent extent : extents) {
            for (int x = extent.minx ; x <= extent.maxx ; ++x) {
                for (int y = extent.miny ; y <= extent.maxy ; ++y) {
                    switch(thing.getCell(x,y)) {
                        case UNSHADED: return LogicStatus.CONTRADICTION;
                        case SHADED: break;
                        case UNKNOWN:
                            result = LogicStatus.LOGICED;
                            thing.setCell(x,y, CellState.SHADED);
                            break;
                    }
                }
            }
        }

        return result;
    }
}
