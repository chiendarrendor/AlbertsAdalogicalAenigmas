import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class FutonPair {
    private static int next_uuid = 0;
    private int uuid;
    private Point pillowEnd;
    private Point futonEnd;
    private Set<Point> adjacents = new HashSet<>();

    private static boolean inBounds(int x,int y,int bwidth,int bheight) {
        if (x < 0) return false;
        if (y < 0) return false;
        if (x >= bwidth) return false;
        if (y >= bheight) return false;
        return true;
    }


    public FutonPair(Point pillowEnd,Point futonEnd,int bwidth,int bheight) {
        this.pillowEnd = pillowEnd;
        this.futonEnd = futonEnd;
        uuid = ++next_uuid;

        for(Direction d: Direction.orthogonals()) {
            Point pillowAdjacent = d.delta(pillowEnd,1);
            Point futonAdjacent = d.delta(futonEnd,1);
            if (inBounds(pillowAdjacent.x,pillowAdjacent.y,bwidth,bheight) && !pillowAdjacent.equals(futonEnd)) adjacents.add(pillowAdjacent);
            if (inBounds(futonAdjacent.x,futonAdjacent.y,bwidth,bheight) && !futonAdjacent.equals(pillowEnd)) adjacents.add(futonAdjacent);
        }
    }

    public int getUuid() { return uuid; }
    public Point getPillow() { return pillowEnd; }
    public Point getFuton() { return futonEnd; }
    public Collection<Point> getAdjacents() { return adjacents; }
}
