import grid.puzzlebits.Direction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CellLines {
    int x;
    int y;
    Map<Direction,String> lines = new HashMap<>();
    Set<String> linenames = new HashSet<>();

    public CellLines(int x,int y) { this.x = x; this.y = y; }

    public void addLine(Direction d,String linename) { lines.put(d,linename); linenames.add(linename); }

    private static Direction[] required = new Direction[] {
            Direction.NORTHWEST, Direction.NORTHEAST,
            Direction.EAST,Direction.WEST,
            Direction.SOUTHEAST,Direction.SOUTHWEST };

    public void verifyLines() {
        if (lines.size()  != 6) throw new RuntimeException("CellLines " + x + " " + y + " does not have 6 entries");
        for(Direction d : required) {
            if (!lines.containsKey(d)) {
                throw new RuntimeException("CellLines " + x + " " + y + " is missing direction " + d);
            }
        }
    }

    public String getCellLine(Direction d) { return lines.get(d); }

    public Set<String> getEdges() { return linenames; }
}
