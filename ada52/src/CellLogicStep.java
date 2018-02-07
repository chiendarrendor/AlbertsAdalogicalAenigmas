import grid.logic.LogicStatus;
import grid.logic.LogicStep;

public class CellLogicStep implements LogicStep<Board>
{
    int x;
    int y;

    public CellLogicStep(int x, int y) { this.x = x; this.y = y; }

    public LogicStatus apply(Board thing)
    {
        if (!thing.getWorkBlock(x,y).isValid()) return LogicStatus.CONTRADICTION;
        return LogicStatus.STYMIED;
    }
}
