import grid.puzzlebits.Direction;
import javafx.scene.control.Cell;

import java.util.HashSet;
import java.util.Set;

public class CellLogicStep {
    private int pathcount;
    private int wallcount;
    private int x;
    private int y;
    Set<Direction> unknowns = new HashSet<>();
    public CellLogicStep(int x,int y) { this.x = x; this.y = y; }

    public void scan(Board b) { scan(b,x,y); }


    public void scan(Board b, int x,int y) {
        unknowns.clear();
        pathcount = 0;
        wallcount = 0;

        for (Direction d : Direction.orthogonals()) {
            switch(b.getEdge(x,y,d)) {
                case PATH: ++pathcount; break;
                case WALL: ++wallcount; break;
                case UNKNOWN: unknowns.add(d); break;
            }
        }
    }

    public int getPathCount() { return pathcount; }
    public int getWallCount() { return wallcount; }
    public Set<Direction> getUnknowns() { return unknowns; }
    public int getX() { return x; }
    public int getY() { return y; }

}
