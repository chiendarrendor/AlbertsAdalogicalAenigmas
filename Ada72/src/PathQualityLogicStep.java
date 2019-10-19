import grid.logic.LogicStatus;
import grid.puzzlebits.Direction;
import grid.puzzlebits.Turns;
import grid.solverrecipes.singleloopflatten.BadMergeException;
import grid.solverrecipes.singleloopflatten.EdgeState;

import java.awt.Point;
import java.util.List;

public class PathQualityLogicStep implements grid.logic.LogicStep<Board> {
    @Override public LogicStatus apply(Board thing) {
        //System.out.println("Starting PQLS");
        try {
            thing.cleanPaths();
        } catch(BadMergeException bme) {
            return LogicStatus.CONTRADICTION;
        }
        //System.out.println("PQLS Path cleaned");

        List<PathInfo> paths = thing.getPathInfo();
        LogicStatus result = LogicStatus.STYMIED;
        //System.out.println("PQLS " + paths.size() + " paths found");

        for (PathInfo pi : paths) {
            LogicStatus r = applyOne(thing,pi);
            if (r == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
            if (r == LogicStatus.LOGICED) result = LogicStatus.LOGICED;
        }

        return result;
    }

    private LogicStatus applyOne(Board thing, PathInfo pi) {
        if (pi.isLoop) return LogicStatus.CONTRADICTION;

        int minnumbends = 1;
        int maxNumBends = Integer.MAX_VALUE;
        int endcount = 0;

        if (pi.end1Terminated && pi.end2Terminated) {
            if (pi.end1Clue > 0 && pi.end2Clue > 0 && pi.end1Clue != pi.end2Clue) return LogicStatus.CONTRADICTION;
        }

        if (pi.end1Terminated) {
            ++endcount;
            if (pi.end1Clue > 0) minnumbends = maxNumBends = pi.end1Clue;
        }

        if (pi.end2Terminated) {
            ++endcount;
            if (pi.end2Clue > 0) minnumbends = maxNumBends = pi.end2Clue;
        }

        if (endcount == 0) return LogicStatus.STYMIED;
        if (endcount == 2) {
            if (pi.bendcount < minnumbends || pi.bendcount > maxNumBends) return LogicStatus.CONTRADICTION;
            return LogicStatus.STYMIED;
        }

        // if we get here, we have only one end, and whatever we know about the number of bends (either [1,MAX_INT), or
        // min = max = clue)
        if (pi.bendcount > maxNumBends) return LogicStatus.CONTRADICTION;

        Point openp = pi.p.endOne();
        if (pi.end1Terminated) openp = pi.p.endTwo();

        // what we know about openp:
        // it has one path entering it (or it wouldn't be the end of path)
        // it's not a terminal (invariants say so)
        // it has 2 or 3 unknown edges

        Point backp = null;
        for (Direction d: Direction.orthogonals()) {
            if (thing.getEdge(openp.x,openp.y,d) == EdgeState.PATH) {
                backp = d.delta(openp,1);
                break;
            }
        }

        LogicStatus result = LogicStatus.STYMIED;
        int posscount = 0;
        for (Direction d: Direction.orthogonals()) {
            EdgeState es = thing.getEdge(openp.x,openp.y,d);
            // only check unknown directions
            if (es != EdgeState.UNKNOWN) continue;

            // if we can't bend, a bend is impossible
            Point forp = d.delta(openp,1);
            boolean isTurn = Turns.isBend(Turns.makeTurn(backp,openp,forp));
            if (isTurn && pi.bendcount == maxNumBends) {
                thing.setEdge(openp.x,openp.y,d,EdgeState.WALL);
                result = LogicStatus.LOGICED;
                continue;
            }

            // this could possibly be interesting, but for now if the adjacent space is not a terminal (clue)
            // let's mark it as possible and continue.
            if (!thing.hasClue(forp.x,forp.y)) {
                ++posscount;
                continue;
            }

            // if we get here, forp points to a clue space.
            int forclue = thing.getClue(forp.x,forp.y);
            // if we have a number and so do they and they don't match, we can't go that way.
            if (forclue > 0 && maxNumBends < Integer.MAX_VALUE && forclue != maxNumBends) {
                thing.setEdge(openp.x,openp.y,d,EdgeState.WALL);
                result = LogicStatus.LOGICED;
                continue;
            }

            // going in the direction of this clue/terminal will end the path...
            // if that is the case, then we need to know if this will be a legal terminal.
            int tmin = minnumbends;
            int tmax = maxNumBends;
            int tcurnum = pi.bendcount + (isTurn ? 1 : 0);
            if (forclue > 0) tmin = tmax = forclue;

            if (tcurnum < tmin || tcurnum > tmax) {
                thing.setEdge(openp.x,openp.y,d,EdgeState.WALL);
                result = LogicStatus.LOGICED;
                continue;
            }

            ++posscount;
        }

        if (posscount == 0) return LogicStatus.CONTRADICTION;
        return result;
    }
}
