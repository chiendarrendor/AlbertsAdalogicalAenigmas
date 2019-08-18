import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RegionLogicStep implements LogicStep<Board> {
    Set<Point> cellset;
    List<List<Point>> pairset =  new ArrayList<>();
    char regid;

    public RegionLogicStep(char regid,Set<Point> regionSet,Board b) {
        cellset = regionSet;

        this.regid = regid;
        int minx = Integer.MAX_VALUE;
        int miny = Integer.MAX_VALUE;
        int maxx = -1;
        int maxy = -1;

        // calculate rectangular convex hull
        for(Point p : regionSet) {
            if (p.x < minx) minx = p.x;
            if (p.y < miny) miny = p.y;
            if (p.x > maxx) maxx = p.x;
            if (p.y > maxy) maxy = p.y;
        }
        Set<Point> checklist = new HashSet<>(regionSet);
        for (int x = minx ; x <= maxx ; ++x) {
            for (int y = miny ; y <= maxy ; ++y) {
                Point tp = new Point(x,y);
                if (!checklist.contains(tp)) continue;
                int dx = x - minx;
                int dy = y - miny;
                Point op = new Point(maxx-dx,maxy-dy);
                if (tp.equals(op)) continue;
                if (!checklist.contains(op)) throw new RuntimeException("Region " + regid + " has non-rotatable pair: " + tp + "," + op);
                List<Point> pair = new ArrayList<>();
                pair.add(tp);
                pair.add(op);
                checklist.remove(tp);
                checklist.remove(op);
                pairset.add(pair);
            }
        }

        for (int i = 0 ; i < pairset.size() ; ++i) {
            for(Point p : pairset.get(i)) {
                b.setRegionPair(p.x,p.y,b.getRegionPair(p.x,p.y)+i);
            }
        }
    }

    private LogicStatus cleanPoint(Point p, Board thing) {
        CellType ct = thing.getCell(p.x,p.y);
        if (ct.isIllegal()) return LogicStatus.CONTRADICTION;
        if (ct.getPresenceType() != PresenceType.UNKNOWN) return LogicStatus.STYMIED;
        if (ct.getPathType() == PathType.TERMINAL) {
            thing.setCell(p.x,p.y,PresenceType.REQUIRED);
            return LogicStatus.LOGICED;
        }
        if (ct.getPathType() != PathType.EMPTY && ct.getPathType() != PathType.INITIAL) {
            thing.setCell(p.x,p.y,PresenceType.FORBIDDEN);
            return LogicStatus.LOGICED;
        }
        return LogicStatus.STYMIED;
    }


    private LogicStatus applyToPair(List<Point> pair,Board thing) {
        Point p1 = pair.get(0);
        Point p2 = pair.get(1);

        LogicStatus result = LogicStatus.STYMIED;

        LogicStatus ls1 = cleanPoint(p1,thing);
        if (ls1 == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
        if (ls1 == LogicStatus.LOGICED) result = LogicStatus.LOGICED;

        LogicStatus ls2 = cleanPoint(p2,thing);
        if (ls2 == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
        if (ls2 == LogicStatus.LOGICED) result = LogicStatus.LOGICED;

        // if we got here, each cell is correct unto itself.  we now need to see if they are a valid pair.
        // 1v 2>        UNK     REQ     FORB
        // UNK          ?       1R      1F
        // REQ          2R      S       C!
        // FORB         2F      C!      S

        CellType ct1 = thing.getCell(p1.x,p1.y);
        CellType ct2 = thing.getCell(p2.x,p2.y);


        if (ct1.getPresenceType() != PresenceType.UNKNOWN && ct2.getPresenceType() != PresenceType.UNKNOWN) {
            return ct1.getPresenceType() == ct2.getPresenceType() ? result : LogicStatus.CONTRADICTION;
        }

        if (ct1.getPresenceType() == PresenceType.UNKNOWN && ct2.getPresenceType() == PresenceType.UNKNOWN) return result;

        if (ct1.getPresenceType() == PresenceType.UNKNOWN) {
            thing.setCell(p1.x,p1.y,ct2.getPresenceType());
            result = LogicStatus.LOGICED;
        }

        if (ct2.getPresenceType() == PresenceType.UNKNOWN) {
            thing.setCell(p2.x,p2.y,ct1.getPresenceType());
            result = LogicStatus.LOGICED;
        }

        return result;
    }



    @Override public LogicStatus apply(Board thing) {
        LogicStatus result = LogicStatus.STYMIED;

        for (List<Point> pair : pairset) {
            LogicStatus onestatus = applyToPair(pair,thing);
            if (onestatus == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
            if (onestatus == LogicStatus.LOGICED) result = LogicStatus.LOGICED;
        }

        int possiblecount = 0;

        for (Point p : cellset) {
            JumpList target = thing.getBackReferences(p.x, p.y);
            List<Jump> valids = target.stillValidJumps(thing);
            possiblecount += valids.size();
        }


        if (possiblecount == 0) return LogicStatus.CONTRADICTION;



        return result;
    }
}
