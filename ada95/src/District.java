import grid.puzzlebits.Direction;
import grid.puzzlebits.EdgeContainer;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

public class District {
    private static int nextId = 1;

    int id;
    boolean isNumbered;
    boolean isBroken = false;
    int number = Integer.MAX_VALUE;
    Set<Point> contents = new HashSet<>();
    Set<EdgeContainer.EdgeCoord> internalUnknownEdges = new HashSet<>();
    Set<EdgeContainer.EdgeCoord> externalUnknownEdges = new HashSet<>();

    public District() { this.id = nextId++; this.isNumbered = false;}
    public District(int number) { this.id = nextId++; this.isNumbered = true; this.number = number; }
    public District(District right) {
        id = right.id;
        isNumbered = right.isNumbered;
        isBroken = right.isBroken;
        number = right.number;
        contents.addAll(right.contents);
        internalUnknownEdges.addAll(right.internalUnknownEdges);
        externalUnknownEdges.addAll(right.externalUnknownEdges);
    }

    public int getId() { return id; }
    // we store edges in Edge Space, but district creation uses cell space...transform here
    public void addEdge(int x, int y, Direction d) {
        EdgeContainer.EdgeCoord ed = new EdgeContainer.EdgeCoord(new EdgeContainer.CellCoord(x,y,d));
        externalUnknownEdges.add(ed);
    }

    // the removal of an edge means that it's no longer unknown...caller will do the right thing, we should trust they
    // know what they're doing.
    public void removeEdge(EdgeContainer.EdgeCoord ec) {
        internalUnknownEdges.remove(ec);
        externalUnknownEdges.remove(ec);
    }

    public void setNumber(int number) {
        isNumbered = true;
        this.number = number;
    }

}
