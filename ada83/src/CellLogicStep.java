import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;
import grid.solverrecipes.singleloopflatten.EdgeState;

import java.util.ArrayList;
import java.util.List;

public class CellLogicStep implements LogicStep<Board> {
    int x;
    int y;
    public CellLogicStep(int x, int y) { this.x = x; this.y = y; }

    @Override public LogicStatus apply(Board thing) {
        return thing.processCell(x,y,CellType.UNKNOWN).result;
    }
}
