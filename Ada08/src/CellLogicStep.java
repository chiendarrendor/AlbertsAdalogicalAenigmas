import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class CellLogicStep implements LogicStep<Board> {
    int x;
    int y;
    public CellLogicStep(int x, int y) { this.x = x ; this.y = y; }


    int wallcount = 0;
    int pathcount = 0;
    List<Direction> unknowns = new ArrayList<>();

    private void setup(Board thing) {
        wallcount = 0;
        pathcount = 0;
        unknowns.clear();

        for (Direction d : Direction.orthogonals()) {
            switch(thing.getEdge(x,y,d)) {
                case WALL: ++wallcount;   break;
                case PATH: ++pathcount; break;
                case UNKNOWN: unknowns.add(d); break;
            }
        }
    }


    public LogicStatus apply(Board thing) {
        setup(thing);
        if (pathcount > 2) return LogicStatus.CONTRADICTION;
        switch(thing.getCell(x,y)) {
            case UNKNOWN: return processUnknown(thing);
            case PATH: return processPath(thing);
            case WALL:
            case ARROW: return processWall(thing);
            default: throw new RuntimeException("Bwa?");
        }
    }

    private LogicStatus processWall(Board thing) {
        if (pathcount > 0) return LogicStatus.CONTRADICTION;
        if (unknowns.size() == 0) return LogicStatus.STYMIED;
        unknowns.stream().forEach(d->thing.setEdge(x,y,d,EdgeState.WALL));
        return LogicStatus.LOGICED;
    }

    private LogicStatus processPath(Board thing) {
        if (wallcount > 2) return LogicStatus.CONTRADICTION;
        if (pathcount + unknowns.size() < 2) return LogicStatus.CONTRADICTION;
        if (unknowns.size() == 0) return LogicStatus.STYMIED;
        if (wallcount == 2) {
            unknowns.stream().forEach(d->thing.setEdge(x,y,d,EdgeState.PATH));
            return LogicStatus.LOGICED;
        }
        if (pathcount + unknowns.size() == 2) {
            unknowns.stream().forEach(d->thing.setEdge(x,y,d,EdgeState.WALL));
            return LogicStatus.LOGICED;
        }

        return LogicStatus.STYMIED;
    }

    private LogicStatus processUnknown(Board thing) {
        if (wallcount > 2) {
            if (pathcount > 0) return LogicStatus.CONTRADICTION;
            thing.setCell(x,y,CellState.WALL);
            return LogicStatus.LOGICED;
        }

        if (pathcount > 0) {
            thing.setCell(x,y,CellState.PATH);
            return LogicStatus.LOGICED;
        }

        return LogicStatus.STYMIED;

    }


}
