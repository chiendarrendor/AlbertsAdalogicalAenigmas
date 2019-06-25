import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Region {
    private char id;
    private Set<Point> cells = new HashSet<>();

    public Set<Point> getCellSet() { return cells; }

    public class OverlappingOmino {
        Point regionCell;
        OminoSet.Omino omino;
        List<Point> setcells = new ArrayList<>();
        public OverlappingOmino(Point cell, OminoSet.Omino omino) {
            this.regionCell = cell; this.omino = omino;
            for (Point p : omino.getPointsOnCenter()) {
                setcells.add(new Point(regionCell.x + p.x,regionCell.y+p.y));
            }
        }
    }

    List<OverlappingOmino> overlaps = new ArrayList<>();



    public Region(char id) { this.id = id; }

    public Region(Region right) {
        id = right.id;
        cells = right.cells;
        overlaps.addAll(right.overlaps);
    }



    public void addCell(int x,int y) { cells.add(new Point(x,y)); }

    public void calculateOverlappingOminoes(OminoSet ominoes, OminoSet.OminoFamily squareOminoFamily) {

        for (Point p : cells) {
            for (OminoSet.Omino o : ominoes.ominoes.values()) {
                if (o.myfamily == squareOminoFamily) continue;
                if (this.contains(p,o)) {
                    overlaps.add(new OverlappingOmino(p,o));
                }
            }
        }
    }

    public boolean contains(Point center, OminoSet.Omino omino) {
        for(Point p : omino.getPointsOnCenter()) {
            if (!cells.contains(new Point(center.x + p.x,center.y + p.y))) return false;
        }
        return true;
    }

    public void showOverlaps() {
        System.out.println("Region: " + id);
        for(OverlappingOmino oo : overlaps) {
            System.out.println("  " + oo.regionCell + ": " + oo.omino.toString());
        }
    }

}
