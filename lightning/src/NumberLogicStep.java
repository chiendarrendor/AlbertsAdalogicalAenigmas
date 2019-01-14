import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class NumberLogicStep implements LogicStep<Board> {
    int id;
    int size;
    List<Point> adjs = new ArrayList<>();

    public NumberLogicStep(int id, int x, int y, SubBoard sb) {
        this.id = id; size = sb.getNumber(x,y);
        Point cen = new Point(x,y);
        for (Direction d: Direction.orthogonals()) {
            Point ap = d.delta(cen,1);
            if (sb.inBounds(ap) && !sb.isBlocker(ap.x,ap.y)) adjs.add(ap);
        }
    }

    @Override public LogicStatus apply(Board thing) {
        SubBoard sb = thing.getSubBoard(id);
        List<Point> unknowns = new ArrayList<>();
        int numlights = 0;
        int numnonlights = 0;

        for(Point p : adjs) {
            switch(sb.getLightState(p.x,p.y)) {
                case LIGHT: ++numlights; break;
                case NOLIGHT: ++numnonlights; break;
                case UNKNOWN: unknowns.add(p);
            }
        }

        if (numlights > size) return LogicStatus.CONTRADICTION;
        if (numlights + unknowns.size() < size) return LogicStatus.CONTRADICTION;
        if (unknowns.size() == 0) return LogicStatus.STYMIED;

        if (numlights == size) {
            unknowns.stream().forEach(p->sb.setLightState(p.x,p.y,LightState.NOLIGHT));
            return LogicStatus.LOGICED;
        }

        if (numlights + unknowns.size() == size) {
            unknowns.stream().forEach(p->sb.setLightState(p.x,p.y,LightState.LIGHT));
            return LogicStatus.LOGICED;
        }

        return LogicStatus.STYMIED;
    }
}
