import com.sun.org.apache.bcel.internal.generic.NOP;
import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;
import grid.puzzlebits.EdgeContainer.CellCoord;
import grid.solverrecipes.singleloopflatten.EdgeState;

import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class RegionEdgeLogicStep implements LogicStep<Board> {
    Region r;
    Map<CellCoord,Character> regionedges = new HashMap<>();

    public RegionEdgeLogicStep(Board b, Region r) {
        this.r = r;

        for (Point p : r.getCells()) {
            for (Direction d: Direction.orthogonals()) {
                Point op = d.delta(p,1);
                if (!b.onBoard(op)) continue;
                char orid = b.getRegionId(op.x,op.y);
                if (orid == r.getId()) continue;
                regionedges.put(new CellCoord(p.x,p.y,d),orid);
            }
        }
    }

    private class EdgeManager {
        Board b;
        int wallcount = 0;
        Set<CellCoord> unknowns = new HashSet<>();
        Set<CellCoord> paths = new HashSet<>();
        CellCoord p1cc = null;
        CellCoord p2cc = null;

        public EdgeManager(Board b) {
            this.b = b;
            for (CellCoord cc : regionedges.keySet()) {
                switch(b.getEdge(cc.x,cc.y,cc.d)) {
                    case UNKNOWN: unknowns.add(cc); break;
                    case PATH:
                        paths.add(cc);
                        if (paths.size() == 1) p1cc = cc;
                        if (paths.size() == 2) p2cc = cc;
                        break;
                    case WALL: ++wallcount; break;
                }
            }
        }

        int pathSize() { return paths.size(); }
        int unknownSize() { return unknowns.size(); }
        CellCoord getP1() { return p1cc; }
        CellCoord getP2() { return p2cc; }

        Set<CellCoord> unknownIter() { return new HashSet<>(unknowns); }

        void makeUnknownPath(CellCoord unk) {
            if (!unknowns.contains(unk)) throw new RuntimeException("Unknown didn't contain unknown!");
            b.setEdge(unk.x,unk.y,unk.d,EdgeState.PATH);
            unknowns.remove(unk);
            paths.add(unk);
            if (paths.size() == 1) p1cc = unk;
            if (paths.size() == 2) p2cc = unk;
        }

        void makeUnknownWall(CellCoord unk) {
            if (!unknowns.contains(unk)) throw new RuntimeException("Unknown didn't contain unknown!");
            b.setEdge(unk.x,unk.y,unk.d,EdgeState.WALL);
            unknowns.remove(unk);
            ++wallcount;
        }

        // if we have 2 paths, all unknowns should be walls
        // if we have one path, all unknowns to the same region should be walls
        int cleanPathDuplicates() {
            if (pathSize() > 2) throw new RuntimeException("cleanPathDuplicates should only be called on valid path list");
            if (pathSize() == 0) return 0;
            int setcount = 0;
            char orid = regionedges.get(p1cc);
            for(CellCoord cc : unknownIter()) {
                if (pathSize() == 2) {
                    // do nothing
                } else { // if pathsize == 1
                    if (regionedges.get(cc) != orid) continue;
                }
                ++setcount;
                makeUnknownWall(cc);
            }
            return setcount;
        }

        boolean pathsToSamePlace() {
            if (pathSize() < 2) return false;
            return regionedges.get(p1cc) == regionedges.get(p2cc);
        }

        LogicStatus dayNightValidate(CellCoord path) {
            char me = r.getId();
            char other = regionedges.get(path);
            RegionStatus mystatus = b.getRegionStatus(me);
            RegionStatus otherstatus = b.getRegionStatus(other);

            // possibles:
            // DU
            // NU
            // UD
            // UN

            if (mystatus == RegionStatus.UNKNOWN && otherstatus == RegionStatus.UNKNOWN) return LogicStatus.STYMIED;
            if (mystatus == RegionStatus.DAY && otherstatus == RegionStatus.DAY) return LogicStatus.CONTRADICTION;
            if (mystatus == RegionStatus.NIGHT && otherstatus == RegionStatus.NIGHT) return LogicStatus.CONTRADICTION;
            if (mystatus == RegionStatus.DAY && otherstatus == RegionStatus.NIGHT) return LogicStatus.STYMIED;
            if (mystatus == RegionStatus.NIGHT && otherstatus == RegionStatus.DAY) return LogicStatus.STYMIED;

            // if we get here, exactly one region is unknown and the other is not.
            if (mystatus == RegionStatus.DAY) b.setRegionStatus(other,RegionStatus.NIGHT);
            else if (mystatus == RegionStatus.NIGHT) b.setRegionStatus(other,RegionStatus.DAY);
            else if (otherstatus == RegionStatus.DAY) b.setRegionStatus(me,RegionStatus.NIGHT);
            else b.setRegionStatus(me,RegionStatus.DAY);

            return LogicStatus.LOGICED;
        }


    }





    @Override public LogicStatus apply(Board thing) {
        EdgeManager em = new EdgeManager(thing);

        if (em.pathSize() > 2) return LogicStatus.CONTRADICTION;
        if (em.pathSize() + em.unknownSize() < 2) return LogicStatus.CONTRADICTION;
        // this does assume that the problemm is complex enough that it has more than 2 regions....
        if (em.pathsToSamePlace()) return LogicStatus.CONTRADICTION;

        LogicStatus result = LogicStatus.STYMIED;

        if (em.cleanPathDuplicates() > 0) result = LogicStatus.LOGICED;

        // so, if we get here:
        // pathsize is 0,1, or 2
        // if pathsize is 2, then we're all walled off.
        // if pathsize is one, then all other edges to that same other region have been walled.
        if (em.unknownSize() > 0) {
            // if we're here, we have 0 or 1 paths, so the only thing to do is to check if
            // we can know our two paths.
            if (em.pathSize() + em.unknownSize() == 2) {
                em.unknownIter().stream().forEach(cc->em.makeUnknownPath(cc));
                result = LogicStatus.LOGICED;
                if (em.pathsToSamePlace()) return LogicStatus.CONTRADICTION;
            }
        }

        // if we have any paths out, validate our region's day/night vs theirs
        if (em.pathSize() > 0) {
            LogicStatus dnv = em.dayNightValidate(em.getP1());
            if (dnv == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
            if (dnv == LogicStatus.LOGICED) result = LogicStatus.LOGICED;
        }
        if (em.pathSize() > 1) {
            LogicStatus dnv = em.dayNightValidate(em.getP2());
            if (dnv == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
            if (dnv == LogicStatus.LOGICED) result = LogicStatus.LOGICED;
        }

        return result;
    }
}
