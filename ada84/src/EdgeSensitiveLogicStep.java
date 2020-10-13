import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public abstract class EdgeSensitiveLogicStep implements LogicStep<Board> {
    int x;
    int y;
    int pathcount;
    int wallcount;
    List<Point> unkowns = new ArrayList<>();
    public EdgeSensitiveLogicStep(int x,int y) { this.x = x; this.y = y; }
    protected void calculate(Board b) {
        unkowns.clear();
        pathcount = 0;
        wallcount = 0;

        for (Direction d : Direction.orthogonals()) {
            Point op = d.delta(x,y,1);
            if (!b.inBounds(op.x,op.y)) { ++wallcount; continue; }
            switch(b.getCell(op.x,op.y)) {
                case PATH: ++pathcount; break;
                case WALL: ++wallcount; break;
                case UNKNOWN: unkowns.add(op);
            }
        }
    }
}
