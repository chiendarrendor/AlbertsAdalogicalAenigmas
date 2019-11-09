import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;
import grid.solverrecipes.singleloopflatten.EdgeState;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class NumberClueCoreLogicStep implements LogicStep<Board> {
    int x;
    int y;
    int number;
    public NumberClueCoreLogicStep(int x, int y, int numberClue) { this.x = x; this.y = y; this.number = numberClue; }

    @Override public LogicStatus apply(Board thing) {
        LogicStatus result = LogicStatus.STYMIED;

        if (thing.getCellType(x,y) == CellType.UNKNOWN) {
            thing.setCellType(x,y,CellType.BEND);
            result = LogicStatus.LOGICED;
        } else if (thing.getCellType(x,y) != CellType.BEND) {
            return LogicStatus.CONTRADICTION;
        }

        return result;
    }
}
