import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CluedRegionLogicStep implements LogicStep<Board> {
    private List<Point> cells;
    int size;
    public CluedRegionLogicStep(List<Point> regionCells,int size) { this.cells = regionCells; this.size = size; }

    @Override public LogicStatus apply(Board thing) {
        int shadecount = 0;
        int unshadecount = 0;
        Set<Point> unknowns = new HashSet<>();

        for (Point p : cells) {
            switch(thing.getCell(p.x,p.y)) {
                case SHADED: ++shadecount;  break;
                case UNSHADED: ++unshadecount; break;
                case UNKNOWN: unknowns.add(p); break;
            }
        }

        if (shadecount > size) return LogicStatus.CONTRADICTION;
        if (shadecount + unknowns.size() < size) return LogicStatus.CONTRADICTION;
        if (unknowns.size() == 0) return LogicStatus.STYMIED;
        if (shadecount == size) {
            unknowns.stream().forEach(p->thing.setCell(p.x,p.y,CellState.UNSHADED));
            return LogicStatus.LOGICED;
        }
        if (shadecount + unknowns.size() == size) {
            unknowns.stream().forEach(p->thing.setCell(p.x,p.y,CellState.SHADED));
            return LogicStatus.LOGICED;
        }

        return LogicStatus.STYMIED;
    }
}
