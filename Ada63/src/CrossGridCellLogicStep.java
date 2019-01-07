import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.util.ArrayList;
import java.util.List;

public class CrossGridCellLogicStep implements LogicStep<Board> {
    int x;
    int y;
    public CrossGridCellLogicStep(int x, int y) { this.x = x; this.y = y; }

    @Override public LogicStatus apply(Board thing) {
        List<Integer> unknowns = new ArrayList<>();
        int blackcount = 0;
        int whitecount = 0;

        for (int bid = 0 ; bid < thing.getBoardCount() ; ++bid) {
            switch(thing.getSubBoard(bid).getCell(x,y)) {
                case UNKNOWN: unknowns.add(bid); break;
                case BLACK: ++blackcount; break;
                case WHITE: ++whitecount; break;
            }
        }

        if (blackcount > 1) return LogicStatus.CONTRADICTION;
        if (unknowns.size() == 0) return LogicStatus.STYMIED;
        if (blackcount == 1) {
            unknowns.stream().forEach((bid)->thing.getSubBoard(bid).setCell(x,y,CellState.WHITE));
            return LogicStatus.LOGICED;
        }

        return LogicStatus.STYMIED;
    }
}
