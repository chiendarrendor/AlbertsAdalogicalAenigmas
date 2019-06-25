import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// for each
public class RegionLogicStep implements LogicStep<Board> {
    char regionid;

    public RegionLogicStep(char c) { regionid = c; }

    private boolean overlaps(Region.OverlappingOmino oo,Board thing) {
        for (Point p : oo.setcells) {
            if (thing.getCellType(p.x,p.y) == CellType.WHITE) return false;
        }
        return true;
    }


    @Override public LogicStatus apply(Board thing) {
        Region curregion = thing.regionsById.get(regionid);

        int blackcount = 0;
        int whitecount = 0;
        int unkcount = 0;

        for (Point p : curregion.getCellSet()) {
            switch(thing.getCellType(p.x,p.y)) {
                case UNKNOWN: ++unkcount; break;
                case BLACK: ++blackcount; break;
                case WHITE: ++whitecount; break;
            }
        }
        if (blackcount > 4) return LogicStatus.CONTRADICTION;
        if (blackcount + unkcount < 4) return LogicStatus.CONTRADICTION;






        Set<Point> mustbewhite = new HashSet<>();
        Set<Point> commonblack = null;
        List<Region.OverlappingOmino> stillvalid = new ArrayList<>();

        mustbewhite.addAll(curregion.getCellSet());

        for(Region.OverlappingOmino oo : curregion.overlaps) {
            if (!overlaps(oo,thing)) continue;
            stillvalid.add(oo);
            // if we get here, this omino can be in this region.
            for(Point p : oo.setcells) {
                mustbewhite.remove(p);
            }
            if (commonblack == null) {
                commonblack = new HashSet<Point>();
                commonblack.addAll(oo.setcells);
            } else {
                commonblack.retainAll(oo.setcells);
            }
        }
        curregion.overlaps = stillvalid;
        if (curregion.overlaps.size() == 0) return LogicStatus.CONTRADICTION;

        LogicStatus result = LogicStatus.STYMIED;

        for (Point p : mustbewhite) {
            CellType curct = thing.getCellType(p.x,p.y);
            if (curct == CellType.BLACK) return LogicStatus.CONTRADICTION;
            if (curct == CellType.WHITE) continue;
            result = LogicStatus.LOGICED;
            thing.setCellType(p.x,p.y,CellType.WHITE);
        }

        for (Point p : commonblack) {
            CellType curct = thing.getCellType(p.x,p.y);
            if (curct == CellType.WHITE) return LogicStatus.CONTRADICTION;
            if (curct == CellType.BLACK) continue;
            result = LogicStatus.LOGICED;
            thing.setCellType(p.x,p.y,CellType.BLACK);
        }
        return result;
    }
}
