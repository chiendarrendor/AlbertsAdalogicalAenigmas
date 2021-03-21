import grid.puzzlebits.Direction;
import grid.puzzlebits.EdgeContainer;
import grid.puzzlebits.PointAdjacency;
import javafx.scene.control.Cell;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RegionInfo {
    public char regionId;
    public Set<Point> cells = new HashSet<>();
    public Set<EdgeContainer.CellCoord> edges = new HashSet<>();
    public TriArticulation triarticulation = null;

    public RegionInfo(char rid) { this.regionId = rid; }

    public void addCell(int x,int y) {
        cells.add(new Point(x,y));
    }
    public void addEdge(int x,int y,Direction d) {
        edges.add(new EdgeContainer.CellCoord(x,y,d));
    }

    public void verifyConnectivity() {
        if (!PointAdjacency.allAdjacent(cells,false))
            throw new RuntimeException("Region " + regionId + " not contiguous");
        triarticulation = TriArticulation.generate(cells);
    }

    public static class CountInfo {
        private int pathcount = 0;
        private int wallcount = 0;
        private List<EdgeContainer.CellCoord> unknowns = new ArrayList<>();
        private List<EdgeContainer.CellCoord> paths = new ArrayList<>();

        public int getPathCount() { return pathcount; }
        public int getWallCount() { return wallcount; }
        public Collection<EdgeContainer.CellCoord> getUnknowns() { return unknowns; }
        public Collection<EdgeContainer.CellCoord> getPaths() { return paths; }
    }

    public CountInfo getCounts(Board b) {
        CountInfo result = new CountInfo();

        for (EdgeContainer.CellCoord cc : edges) {
            switch(b.getEdge(cc.x,cc.y,cc.d)) {
                case PATH:
                    result.pathcount++;
                    result.paths.add(cc);
                    break;
                case WALL: result.wallcount++; break;
                case UNKNOWN: result.unknowns.add(cc); break;
            }
        }

        return result;
    }


}
