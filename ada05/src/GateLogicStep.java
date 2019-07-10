import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;
import grid.solverrecipes.singleloopflatten.EdgeState;
import org.omg.CORBA.UNKNOWN;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class GateLogicStep implements LogicStep<Board> {
    private static Direction[] virtdirs = new Direction[] { Direction.NORTH,Direction.SOUTH };
    private static Direction[] hordirs = new Direction[] { Direction.WEST,Direction.EAST };


    int id;
    Direction[] walldirs;
    Direction[] pathdirs;

    public GateLogicStep(Gate g) {
        id = g.getId();
        if (g.getOrientation() == '|') {
            pathdirs = hordirs;
            walldirs = virtdirs;
        } else {
            pathdirs = virtdirs;
            walldirs = hordirs;
        }
    }

    private enum CellClassification { WALL, PATH, UNKNOWN, ILLEGAL }

    private CellClassification classify(Board thing, Point p, Direction[] opdirs) {
        int wallcount = 0;
        int pathcount = 0;
        int unkcount = 0;

        for (Direction d : opdirs) {
            switch(thing.getEdge(p.x,p.y,d)) {
                case WALL: ++wallcount; break;
                case PATH: ++pathcount; break;
                case UNKNOWN: ++unkcount; break;
            }
        }
        if (wallcount > 0 && pathcount > 0) return CellClassification.ILLEGAL;
        if (wallcount > 0) return CellClassification.WALL;
        if (pathcount > 0) return CellClassification.PATH;
        return CellClassification.UNKNOWN;
    }



    @Override public LogicStatus apply(Board thing) {
        Gate g = thing.getGate(id);
        LogicStatus result = LogicStatus.STYMIED;

        // 1) cross-orientation edges must be walls
        for (Point p : g.getCells()) {
            for (Direction d : walldirs) {
                switch(thing.getEdge(p.x,p.y,d)) {
                    case PATH:
                        return LogicStatus.CONTRADICTION;
                    case WALL:
                        break;
                    case UNKNOWN:
                        thing.setEdge(p.x,p.y,d, EdgeState.WALL);
                        result = LogicStatus.LOGICED;
                        break;
                }
            }
        }

        // 2) only one cell may contain a path
        List<Point> blockcells = new ArrayList<>();
        List<Point> pathcells = new ArrayList<>();
        List<Point> unkcells = new ArrayList<>();


        for (Point p : g.getCells()) {
            switch(classify(thing,p,pathdirs)) {
                case ILLEGAL: return LogicStatus.CONTRADICTION;
                case PATH: pathcells.add(p); break;
                case WALL: blockcells.add(p); break;
                case UNKNOWN: unkcells.add(p); break;
            }
        }

        if (pathcells.size() > 1) return LogicStatus.CONTRADICTION;
        if (pathcells.size() + unkcells.size() == 0) return LogicStatus.CONTRADICTION;
        if (unkcells.size() == 0) return result;

        if (pathcells.size() == 0 && unkcells.size() == 1) {
            Point up = unkcells.get(0);
            for (Direction d : pathdirs) {
                thing.setEdge(up.x,up.y,d,EdgeState.PATH);
                result = LogicStatus.LOGICED;
            }
        }

        if (pathcells.size() == 1) {
            for (Point p : unkcells) {
                for (Direction d : pathdirs) {
                    thing.setEdge(p.x,p.y,d,EdgeState.WALL);
                    result = LogicStatus.LOGICED;
                }
            }
        }

        return result;
    }
}
