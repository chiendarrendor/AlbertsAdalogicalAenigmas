import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CellLogicStep implements LogicStep<Board> {
    int x;
    int y;
    List<Point> adjacents = new ArrayList<>();

    public CellLogicStep(Board b, int x, int y) {
        this.x = x;
        this.y = y;
        for(Direction d: Direction.orthogonals()) {
            Point np = d.delta(x,y,1);
            if (b.inBounds(np.x,np.y)) adjacents.add(np);
        }
    }

    // logic points:
    // number in this cell = # of adjacent path cells
    // number in this cell is different than numbers in any adjacent cells

    @Override public LogicStatus apply(Board thing) {
        CellData cd = thing.getCellData(x,y);
        if (!cd.isValid()) return LogicStatus.CONTRADICTION;
        if (cd.isWall()) return LogicStatus.STYMIED;

        // if we get here...cell is probably a path, but could possibly be a wall if has(WALLVALUE) is true
        // let's process info about adjacents
        Set<Integer> adjacentKnownCounts  = new HashSet<>();
        int adjacentwallcount = 0;
        int adjacentpathcount = 0;
        List<Point> unknowns = new ArrayList<>();

        for (Point p : adjacents) {
            CellData acd = thing.getCellData(p.x,p.y);
            if (!acd.isValid()) return LogicStatus.CONTRADICTION;
            if (acd.isWall()) {
                ++adjacentwallcount;
                continue;
            } if (acd.isPath()) {
                ++adjacentpathcount;
                if (acd.isComplete()) {
                    adjacentKnownCounts.add(acd.getValue());
                }
                continue;
            }
            unknowns.add(p);
        }

        LogicStatus result = LogicStatus.STYMIED;
        // any adjacent known counts are numbers we can't be
        for (int adjval : adjacentKnownCounts) {
            if (cd.has(adjval)) {
                cd.clear(adjval);
                result = LogicStatus.LOGICED;
            }
        }
        // we can't be a count that is smaller than the # of known adjacent paths or larger than adjacent paths + unknowns
        for (int idx = 1 ; idx <= 4 ; ++idx) {
            if (!cd.has(idx)) continue;
            if (idx < adjacentpathcount) {
                cd.clear(idx);
                result = LogicStatus.LOGICED;
            }
            if (idx > adjacentpathcount + unknowns.size()) {
                cd.clear(idx);
                result = LogicStatus.LOGICED;
            }
        }

        if (!cd.isValid()) return LogicStatus.CONTRADICTION;
        if (cd.isWall()) return result;
        // if we are not a wall, but still have wall, that means we do not know if we are PATH or not, and so
        // forcing adjacent spaces would be inappropriate.
        if (cd.has(CellData.WALLVALUE)) return result;
        // if we get here, we know we're a path, and the only legal numbers we contain are between
        // # of adjacent paths and # of adjacent paths + # of unknowns
        // (and none of our numbers match any known adjacents)
        if (!cd.isComplete()) return result;
        // if we get here, we know what number we are.
        if (unknowns.size() == 0) return result;
        // if we get here, we might be able to alter the adjacent unknowns.

        int val = cd.getValue();
        if (val == adjacentpathcount) {
            unknowns.stream().forEach(p->thing.getCellData(p.x,p.y).set(CellData.WALLVALUE));
            result = LogicStatus.LOGICED;
        }
        if (val == adjacentpathcount + unknowns.size()) {
            unknowns.stream().forEach(p->thing.getCellData(p.x,p.y).clear(CellData.WALLVALUE));
            result = LogicStatus.LOGICED;
        }

        return result;
    }
}
