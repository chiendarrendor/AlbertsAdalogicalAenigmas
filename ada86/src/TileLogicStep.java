import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;
import javafx.geometry.Pos;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

public class TileLogicStep implements LogicStep<Board> {
    int basex;
    int basey;

    public TileLogicStep(int x, int y) { basex = x; basey = y; }

    private static LogicStatus processRing(Board thing,int cx,int cy,int dist,boolean doset,Set<Direction> tiledirs) {
        Set<PossibleDestination> doomed = new HashSet<>();


        for (Direction d : Direction.orthogonals()) {
            Point p = d.delta(cx,cy,dist);
            if (!thing.inBounds(p)) continue;
            DestinationSet ds = thing.getCellDestinations(p.x,p.y);

            if (doset) {
                if (tiledirs.contains(d)) {
                    for (PossibleDestination pd : ds.destinations) {
                        if (pd.isBlank()) { doomed.add(pd); continue; }
                        if (pd.destx != p.x || pd.desty != p.y) doomed.add(pd);
                    }
                } else {
                    for (PossibleDestination pd : ds.destinations) {
                        if (pd.isBlank()) continue;
                        if (pd.destx == p.x && pd.desty == p.y) doomed.add(pd);
                    }
                }
            } else {
                for (PossibleDestination pd : ds.destinations) {
                    if (pd.isBlank()) continue;
                    if (pd.destx == p.x && pd.desty == p.y) doomed.add(pd);
                }
            }
        }

        if (doomed.size() == 0) return LogicStatus.STYMIED;
        for(PossibleDestination pd : doomed) thing.clearDestination(pd);
        return LogicStatus.LOGICED;
    }





    @Override public LogicStatus apply(Board thing) {
        DestinationSet cellds = thing.getCellDestinations(basex,basey);
        if (cellds.size() == 0) return LogicStatus.CONTRADICTION;
        if (cellds.size() > 1) return LogicStatus.STYMIED;

        Set<Direction> tiledirs = thing.tileDirections(basex,basey);
        PossibleDestination pd = cellds.getOne();

        // we have to look at the world from the point of view of where the tile is now
        int x = pd.destx;
        int y = pd.desty;

        int maxdist = Math.max(thing.getWidth(),thing.getHeight());
        LogicStatus result = LogicStatus.STYMIED;

        // starting from the adjacent spaces and moving outwards, for each distance
        // we are looking for the smallest possible ring that meets approval
        //   for each direction, determine one of :
                // a) space is _terminal_ (off board)
                // x) space does not have at least one PD: CONTRADICTION
                // b) space _cannot have_ tile (all tiles are either isBlank, or do not end in that space)
                // c) space _must have_ tile (only tiles available are non isBlank and  end in that space)
                // d) space _might have_ tile (any other case)
                // (a,x,b,c,d implemmented in Board::classifyDestinations()

                // if tiledirs _has_ this direction:
                //     case a) this board state is CONTRADICTION
                //     case b) this ring _cannot_ be approved
                //     case c) this ring _must_ be approved
                //     case d) this ring is acceptable
                // if tiledirs _does not have_ this direction
                //     case a) this ring is acceptable
                //     case b) this ring is acceptable
                //     case c) this board state is CONTRADICTION
                //     case d) this ring is acceptable

                // any CONTRADICTION immediately exits algorithm CONTRADICTION
                // combination of _cannot_ and _must be_ immediately exits algorithm CONTRADICTION
                // all _acceptable_ -> end algorithm returning logiced status
                // all _terminal_ -> end algorithm returning logiced status (this only happens if we have no tiledirs)
                // at least one _cannot_ -> go on to next ring (or if no tiledirs)
                //    LOGICED:  all cells in all directions must have all PDs that end there removed (ignore blank)
                // at least one _must_ -> terminate algorithm
                //    LOGICED: all cells in non tiledirs directions must have all PD's that end there removed (ignore blank)
                //             all cells in tiledirs directions must have all PD's that _don't_ end there removed (+blank)

        for (int i = 1 ; ; ++i) {
            boolean must = false;
            boolean cannot = false;
            int termcount = 0;
            for (Direction d : Direction.orthogonals()) {
                Point oppoint = d.delta(x,y,i);
                DestinationSetClassifier dsc = thing.classifyDestinations(oppoint.x,oppoint.y);
                if (tiledirs.contains(d)) {
                    switch(dsc) {
                        case TERMINAL: return LogicStatus.CONTRADICTION;
                        case INVALID: return LogicStatus.CONTRADICTION;
                        case CANNOT_HAVE: cannot = true; break;
                        case MUST_HAVE: must = true; break;
                        case MIGHT_HAVE: break;
                    }
                } else {
                    switch(dsc) {
                        case TERMINAL: ++termcount; break;
                        case INVALID: return LogicStatus.CONTRADICTION;
                        case CANNOT_HAVE: break;
                        case MUST_HAVE: return LogicStatus.CONTRADICTION;
                        case MIGHT_HAVE: break;
                    }
                }
                if (must && cannot) return LogicStatus.CONTRADICTION;
            }
            if (termcount == 4) break;
            // this is okay because without any tiledirs, both must and cannot are false)
            if (tiledirs.size() == 0) cannot = true;

            // so if we get here, at most one of must and cannot are true
            if (!must && !cannot) break;  // this is the 'acceptable' case

            if (must) {
                LogicStatus ls = processRing(thing,x,y,i,true,tiledirs);
                if (ls == LogicStatus.LOGICED) result = LogicStatus.LOGICED;
                break;
            }

            if (cannot) {
                LogicStatus ls = processRing(thing,x,y,i,false,tiledirs);
                if (ls == LogicStatus.LOGICED) result = LogicStatus.LOGICED;
            }
        }
        return result;
    }
}
