import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class OnPathLogicStep implements LogicStep<Board> {
    Point mypoint;
    List<Point> adjacents = new ArrayList<>();
    int autowall = 0;
    public OnPathLogicStep(Board b, Point p) {
        mypoint = p;
        for (Direction d : Direction.orthogonals()) {
            Point np = d.delta(p,1);
            if (b.inBounds(np)) {
                adjacents.add(np);
            } else {
                ++autowall;
            }
        }
    }

    List<Point> unknowns = new ArrayList<>();
    List<Point> paths = new ArrayList<>();
    int numwalls;
    int numonpath;

    public LogicStatus apply(Board thing) {
        if (thing.getCell(mypoint.x,mypoint.y) != CellType.PATH) return LogicStatus.STYMIED;

        unknowns.clear();
        paths.clear();
        numwalls = autowall;
        numonpath = 0;

        for (Point p : adjacents) {
            switch (thing.getCell(p.x, p.y)) {
                case UNKNOWN:
                    unknowns.add(p);
                    break;
                case WALL:
                    ++numwalls;
                    break;
                case PATH:
                    if (thing.isOnPath(p.x,p.y)) ++numonpath;
                    else if (thing.notOnPath(p.x, p.y)) ++numwalls;
                    else paths.add(p);
                    break;
            }
        }

        LogicStatus result = LogicStatus.STYMIED;

        if (thing.getPath(mypoint.x, mypoint.y) == PathStatus.ONPATH) {
            LogicStatus lstat = CheckOnPath(thing);
            if (lstat == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
            if (lstat == LogicStatus.LOGICED) result = LogicStatus.LOGICED;
        } else if (thing.getPath(mypoint.x,mypoint.y) == PathStatus.PATH_TERMINAL) {
            LogicStatus lstat = CheckTerminal(thing);
            if (lstat == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
            if (lstat == LogicStatus.LOGICED) result = LogicStatus.LOGICED;
        }  else {
            LogicStatus lstat = CheckOffPath(thing);
            if (lstat == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
            if (lstat == LogicStatus.LOGICED) result = LogicStatus.LOGICED;
        }



        return result;
    }

    private LogicStatus CheckTerminal(Board thing) {
        if (numonpath > 1) return LogicStatus.CONTRADICTION;
        if (numwalls > 3) return LogicStatus.CONTRADICTION;
        if (unknowns.size() == 0) return LogicStatus.STYMIED;


        LogicStatus result = LogicStatus.STYMIED;
        // unknowns -- set of unknown cells
        // paths -- set of cells of type PATH that are not onPath and _not_ notOnPath
        return result;
    }

    private LogicStatus CheckOffPath(Board thing) {
        if (numwalls < 3) return LogicStatus.STYMIED;
        if (thing.notOnPath(mypoint.x,mypoint.y)) return LogicStatus.STYMIED;
        thing.setNotOnPath(mypoint.x,mypoint.y);
        return LogicStatus.LOGICED;

    }



     private LogicStatus CheckOnPath(Board thing) {
        if (numonpath > 2) return LogicStatus.CONTRADICTION;
        if (numwalls > 2) return LogicStatus.CONTRADICTION;
        if (numwalls < 2) return LogicStatus.STYMIED;

        // if we get here, we have exactly two walls.
        // the other two edges must not only lead to PATH cells,
        // but they are also ONPATH
        LogicStatus result = LogicStatus.STYMIED;

        for (Point p : unknowns) {
            thing.setCell(p.x,p.y,CellType.PATH);
            paths.add(p);
            result = LogicStatus.LOGICED;
        }

        for (Point p : paths) {
            thing.setOnPath(p.x,p.y);
            result = LogicStatus.LOGICED;
        }

        return result;
    }
}
