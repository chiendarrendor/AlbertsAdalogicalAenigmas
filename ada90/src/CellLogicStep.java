import grid.logic.LogicStatus;
import grid.logic.LogicStep;

public class CellLogicStep implements LogicStep<Board> {
    int x;
    int y;
    public CellLogicStep(int x, int y) { this.x = x; this.y = y;  }

    @Override public LogicStatus apply(Board thing) {
        CellState cs = thing.getCellState(x,y);
        if (cs == CellState.UNKNOWN) return LogicStatus.STYMIED;

        if (cs == CellState.WALL) {

        }




        return null;
    }
}
