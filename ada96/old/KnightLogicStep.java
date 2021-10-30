import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class KnightLogicStep implements LogicStep<Board> {
    int knightid;

    public KnightLogicStep(int knightid) { this.knightid = knightid; }

    private boolean pathValid(Board thing, Knight knight, int pathid) {
        // paths that don't move can't be invalidated by the board position
        if (pathid == knight.zeroId()) return true;
        KnightsPath path = knight.getPath(pathid);


        // the zeroth element's board position cannot invalidate the path, so we ignore it.
        for (int i = 1 ; i < path.size() ; ++i) {
            boolean isTerminal = i == path.size()-1;
            Point p = path.locations.get(i);
            switch(thing.getCell(p.x,p.y)) {
                case UNKNOWN: break;
                case POSITION_INITIAL: return false;
                case POSITION_INTERMEDIATE: return false;
                case POSITION_FINAL: return false;
                case CANT_STOP_HERE:
                    if (isTerminal) return false;
                    break;
                case MUST_HAVE_KNIGHT:
                    if (!isTerminal) return false;
                    break;
            }
        }

        if (!thing.getMasterSegmentSet().addable(path.segments)) return false;

        return true;
    }

    private void placePath(Board thing, Knight knight, KnightsPath path) {
        for (int i = 0 ; i < path.size()-1 ; ++i) {
            Point pp = path.locations.get(i);
            thing.setCell(pp.x,pp.y,CellState.POSITION_INTERMEDIATE);
        }
        Point pp = path.locations.get(path.size()-1);
        thing.setCell(pp.x,pp.y,CellState.POSITION_FINAL);
    }





    @Override public LogicStatus apply(Board thing) {
        Knight knight = thing.getKnight(knightid);
        if (knight.isLocked()) return LogicStatus.STYMIED;
        Point initial = knight.getInitial();

        // doing this to prevent delete on list we are iterating on.
        List<Integer> pathids = new ArrayList<>();
        pathids.addAll(knight.getPathIds());

        LogicStatus result = LogicStatus.STYMIED;

        for (int pathid : pathids) {
            if (pathValid(thing,knight,pathid)) continue;

            // if we get here, path isn't valid.
            result = LogicStatus.LOGICED;
            if (pathid == knight.zeroId()) thing.setCell(initial.x,initial.y,CellState.POSITION_INTERMEDIATE);
            knight.clearPath(pathid);
        }

        if (knight.numPaths() == 0) return LogicStatus.CONTRADICTION;

        if (knight.numPaths() == 1) {
            result = LogicStatus.LOGICED;
            KnightsPath path = knight.getPaths().iterator().next();
            knight.lock();
            thing.getMasterSegmentSet().addSegmentSet(path.segments);
            placePath(thing,knight,path);
        }

        return result;
    }
}
