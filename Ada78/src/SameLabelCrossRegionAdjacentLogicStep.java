import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class SameLabelCrossRegionAdjacentLogicStep implements LogicStep<Board> {
    List<Point> pair = new ArrayList<>();
    List<Character> regions = new ArrayList<>();
    public SameLabelCrossRegionAdjacentLogicStep(Board b,int x, int y, Point op) {
        pair.add(new Point(x,y));
        pair.add(op);

        regions.add(b.getRegionId(x,y));
        regions.add(b.getRegionId(op));
    }

    @Override public LogicStatus apply(Board thing) {
        if( pair.stream().anyMatch(p->thing.getCell(p.x,p.y)==CellState.BARRIER)) return LogicStatus.STYMIED;
        if (regions.stream().anyMatch(r->thing.liveLabelCount(r) < 0)) return LogicStatus.STYMIED;
        if (thing.liveLabelCount(regions.get(0)) != thing.liveLabelCount(regions.get(1))) return LogicStatus.STYMIED;
        // if we get here:
        // neither cell is BARRIER
        // neither cell is unkwnown live label caount
        // both cells have the same label count
        if (pair.stream().allMatch(p->thing.getCell(p.x,p.y) == CellState.LABEL)) return LogicStatus.CONTRADICTION;
        if (pair.stream().allMatch(p->thing.getCell(p.x,p.y) == CellState.UNKNOWN)) return LogicStatus.STYMIED;
        // if we get here, then both of them are not label, one of them is unknown
        pair.stream().filter(p->thing.getCell(p.x,p.y) == CellState.UNKNOWN).forEach(p->thing.setCell(p.x,p.y,CellState.BARRIER));
        return LogicStatus.LOGICED;
    }
}
