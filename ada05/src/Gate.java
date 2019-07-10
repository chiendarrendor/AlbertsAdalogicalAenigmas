import grid.puzzlebits.Direction;
import grid.puzzlebits.EdgeContainer;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Gate {
    private static int nextid = 0;

    private char orientation;
    private int id;
    private Set<Point> cells = new HashSet<>();
    private List<EdgeContainer.CellCoord> terminals = new ArrayList<>();
    private Map<Direction,Character> dirchars = new HashMap<>();

    private int number = -1;
    public boolean isNumbered() { return number != -1; }
    public void setNumber(int number) { this.number = number; }
    public int getNumber() { return number; }

    public void addLetter(Direction d, char c) { dirchars.put(d,c); }
    public char getLetter(Direction d) { return dirchars.get(d); }
    public boolean hasLetter(Direction d) { return dirchars.containsKey(d); }


    public Collection<EdgeContainer.CellCoord> getTerminals() { return terminals; }

    public Gate(char orientation) {
        this.orientation = orientation;
        this.id = ++nextid;
    }

    public void addCell(Point p) { cells.add(p); }
    public void addTerminal(EdgeContainer.CellCoord cc) { terminals.add(cc); }
    public char getOrientation() { return orientation; }
    public int getId() { return id; }
    public Collection<Point> getCells() { return cells; }
}
