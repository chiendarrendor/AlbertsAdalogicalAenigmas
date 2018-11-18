import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Region {
    int regionid;
    LetterContainer letcon = null;
    Map<Integer,Edge> edges = new HashMap<>();
    Set<Point> cells = new HashSet<>();
    RegionPair pair = null; // this only gets set if we have a pair.

    public Region(int regionid) { this.regionid = regionid; }
    public Region(Region right) {
        this.regionid = right.regionid;
        this.letcon = right.letcon;
        this.cells = right.cells;
        this.pair = right.pair;
        // edges has to be handled separately to properly construct the graph.
    }

    public boolean hasNeighbor(int nid) { return edges.containsKey(nid); }
    public void addNeighbor(int nid,Edge neighbor) { edges.put(nid,neighbor); }

    public void addCell(int x,int y) { cells.add(new Point(x,y)); }
    public void setLetter(int x,int y,char letter,Direction letterdir) {
        this.letcon = new LetterContainer(x,y,letter,letterdir);
    }

    public LetterContainer getLetterContainer() { return letcon; }

    public void setEdge(int x,int y,Direction d,int nid) { edges.get(nid).addEdge(x,y,d); }

    public void setPair(int other) {
        pair = edges.get(other).pair;
    }

    public boolean isPaired() {
        return pair != null;
    }

}
