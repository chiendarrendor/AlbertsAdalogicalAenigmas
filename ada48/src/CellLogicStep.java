import grid.logic.LogicStatus;
import grid.logic.LogicStep;

/**
 * Created by chien on 10/14/2017.
 */
public class CellLogicStep implements LogicStep<Board>
{
    int x;
    int y;
    public CellLogicStep(int x, int y) { this.x = x; this.y = y; }

    public LogicStatus apply(Board thing)
    {
        Cell c = thing.getCell(x,y);
        if (c.isEmpty()) return LogicStatus.CONTRADICTION;
        if (c.isFixed()) return LogicStatus.STYMIED;
        if (!c.isSingular()) return LogicStatus.STYMIED;

        c.fixed = true;
        return LogicStatus.LOGICED;
    }
}
