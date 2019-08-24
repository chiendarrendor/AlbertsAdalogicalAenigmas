import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

public class LineLiarsLogicStep implements LogicStep<Board> {
    Set<Point> gifts = new HashSet<>();
    int mincount;
    int maxcount;
    public LineLiarsLogicStep(Board b, int sx, int sy, Direction dir, int count, int mincount, int maxcount) {
        for (int i = 0 ; i < count ; ++i) {
            Point p = dir.delta(sx,sy,i);
            if (b.hasNumber(p.x,p.y)) gifts.add(p);
        }
        this.mincount = mincount;
        this.maxcount = maxcount;
    }

    @Override public LogicStatus apply(Board thing) {
        int realcount = 0;
        int fakecount = 0;
        Set<Point> unknowns = new HashSet<>();

        for (Point p : gifts) {
            switch(thing.getHandler(p.x,p.y).realness) {
                case REAL: ++realcount; break;
                case FAKE: ++fakecount; break;
                case UNKNOWN: unknowns.add(p); break;
            }
        }

        if (fakecount > maxcount) return LogicStatus.CONTRADICTION;
        if (fakecount + unknowns.size() < mincount) return LogicStatus.CONTRADICTION;
        if (unknowns.size() == 0) return LogicStatus.STYMIED;

        if (fakecount == maxcount) {
            unknowns.stream().forEach(p->thing.getHandler(p.x,p.y).realness = Realness.REAL);
            return LogicStatus.LOGICED;
        }

        if (fakecount + unknowns.size() == mincount) {
            unknowns.stream().forEach(p->thing.getHandler(p.x,p.y).realness = Realness.FAKE);
            return LogicStatus.LOGICED;
        }


        return LogicStatus.STYMIED;
    }
}
