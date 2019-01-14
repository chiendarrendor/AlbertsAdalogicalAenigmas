import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;
import sun.rmi.runtime.Log;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class CellLogicStep implements LogicStep<Board> {
    int id;
    Point me;
    List<Point> insights = new ArrayList<>();

    public CellLogicStep(int id, int x, int y, SubBoard sb) {
        this.id = id;
        this.me = new Point(x,y);

        for (Direction d : Direction.orthogonals()) {
            int idx = 1;
            while(true) {
                Point ap = d.delta(me,idx);
                if (!sb.inBounds(ap)) break;
                if (sb.isBlocker(ap.x,ap.y)) break;
                insights.add(ap);
                ++idx;
            }
        }
    }

    // for me, always, at least one of me + insights must be a light.
    private LogicStatus amILit(SubBoard sb) {
        List<Point> unknowns = new ArrayList<>();
        int lightcount = 0;
        int darkcount = 0;

        switch(sb.getLightState(me.x,me.y)) {
            case LIGHT: ++lightcount; break;
            case NOLIGHT: ++darkcount; break;
            case UNKNOWN: unknowns.add(me); break;
        }

        for(Point p : insights) {
            switch(sb.getLightState(p.x,p.y)) {
                case LIGHT: ++lightcount; break;
                case NOLIGHT: ++darkcount; break;
                case UNKNOWN: unknowns.add(p); break;
            }
        }

        if (lightcount + unknowns.size() == 0) return LogicStatus.CONTRADICTION;
        if (unknowns.size() == 0) return LogicStatus.STYMIED;
        if (lightcount == 0 && unknowns.size() == 1) {
            unknowns.stream().forEach(p->sb.setLightState(p.x,p.y,LightState.LIGHT));
            return LogicStatus.LOGICED;
        }

        return LogicStatus.STYMIED;
    }

    // if I am a light, no one else I can see can be either.
    private LogicStatus lightCannotBeLit(SubBoard sb) {
        LogicStatus result = LogicStatus.STYMIED;
        if (sb.getLightState(me.x,me.y) != LightState.LIGHT) return LogicStatus.STYMIED;

        for (Point p: insights) {
            switch (sb.getLightState(p.x,p.y)) {
                case NOLIGHT: break;
                case LIGHT: return LogicStatus.CONTRADICTION;
                case UNKNOWN:
                    sb.setLightState(p.x,p.y,LightState.NOLIGHT);
                    result = LogicStatus.LOGICED;
                    break;
            }
        }
        return result;
    }






    @Override public LogicStatus apply(Board thing) {
        LogicStatus result = LogicStatus.STYMIED;
        SubBoard sb = thing.getSubBoard(id);

        LogicStatus lsa = amILit(sb);
        if (lsa == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
        if (lsa == LogicStatus.LOGICED) result = LogicStatus.LOGICED;

        LogicStatus lsb = lightCannotBeLit(sb);
        if (lsb == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
        if (lsb == LogicStatus.LOGICED) result = LogicStatus.LOGICED;

        return result;
    }
}
