import grid.file.GridFileReader;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PossibleDestination {
    static private int nextId = 0;

    static private Board master;
    static void SetMaster(Board master) { PossibleDestination.master = master; }

    int myid;
    int sourcex;
    int sourcey;
    int destx;
    int desty;
    private boolean isBlank;
    boolean stationary = true;
    List<Point> intermediates = new ArrayList<Point>();
    Point source;
    Direction d = null;

    public PossibleDestination(int x,int y) {
        this(x,y,false);
    }


    public PossibleDestination(int x, int y,boolean isBlank) {
        myid = ++nextId;
        sourcex = destx = x;
        sourcey = desty = y;
        source = new Point(sourcex,sourcey);
        this.isBlank = isBlank;
    }

    public PossibleDestination(PossibleDestination right) {
        myid = ++nextId;
        sourcex = right.sourcex;
        sourcey = right.sourcey;
        destx = right.destx;
        desty = right.desty;
        stationary = right.stationary;
        intermediates.addAll(right.intermediates);
        source = right.source;
        isBlank = right.isBlank;
        d = right.d;
    }

    public Point getSource() { return source; }


    public void addPoint(int x,int y) {
        if (!stationary) {
            intermediates.add(new Point(destx,desty));
        } else {
            d = Direction.fromTo(sourcex,sourcey,x,y);
        }
        destx = x;
        desty = y;
        stationary = false;
    }

    public char getLetter() { return master.tileLetter(sourcex,sourcey); }
    public boolean pointsTo(Direction d) {return master.tileDirection(sourcex,sourcey,d); }
    public Set<Direction> tileDirections() { return master.tileDirections(sourcex,sourcey); }
    public boolean isBlank() { return isBlank; }
    public Direction getMoveDirection() { return d; }
    public int getMoveDistance() { return Math.abs(desty-sourcey) + Math.abs(destx-sourcex); }


}
