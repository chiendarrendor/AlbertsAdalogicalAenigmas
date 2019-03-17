import grid.puzzlebits.Direction;
import grid.puzzlebits.EdgeContainer;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

public class LargeCell {
    int cluesize;
    Set<Point> cells;
    Set<EdgeContainer.EdgeCoord> edges = new HashSet<>();
    Set<String> estrings = new HashSet<>();

    public LargeCell(int cluesize,Set<Point> cells) {
        this.cluesize = cluesize;
        this.cells = cells;

        // tricky bit...we want this edges set to contain only those edges that are not between our cells
        // if they are between our cells, they will be mentioned exactly twice.
        // any edge not between our cells will be mentioned exactly once.
        for (Point p : cells) {
            for (Direction d : Direction.orthogonals()) {
                EdgeContainer.EdgeCoord ec = EdgeContainer.getEdgeCoord(p.x,p.y,d);
                if (edges.contains(ec)) edges.remove(ec);
                else edges.add(ec);
            }
        }

        for (EdgeContainer.EdgeCoord ec : edges) {
            estrings.add(CellEdgeTranslator.edgeName(ec));
        }
    }

    public Set<String> getEdgeIdentifiers() { return estrings; }
}
