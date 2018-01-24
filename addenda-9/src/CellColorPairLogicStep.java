import grid.logic.LogicStatus;
import grid.logic.LogicStep;

public class CellColorPairLogicStep implements LogicStep<Board>
{
    int x1;
    int y1;
    int x2;
    int y2;

    public CellColorPairLogicStep(int x1, int y1, int x2, int y2) { this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2; }

    public LogicStatus apply(Board thing)
    {
        CellColor c1 = thing.getCellColor(x1,y1);
        CellColor c2 = thing.getCellColor(x2,y2);

        // 9 cases
        //              c1
        //              BLACK    WHITE   ?
        // c2   BLACK   x        ok      c1=WHITE
        //      WHITE   ok       x       c1=BLACK
        //      ?       c2=WHITE c2=BLACK  ?

        if (c1 == CellColor.UNKNOWN && c2 == CellColor.UNKNOWN) return LogicStatus.STYMIED;
        if (c1 == CellColor.BLACK && c2 == CellColor.WHITE) return LogicStatus.STYMIED;
        if (c1 == CellColor.WHITE && c2 == CellColor.BLACK) return LogicStatus.STYMIED;
        if (c1 == CellColor.WHITE && c2 == CellColor.WHITE) return LogicStatus.CONTRADICTION;
        if (c1 == CellColor.BLACK && c2 == CellColor.BLACK) return LogicStatus.CONTRADICTION;

        if (c1 == CellColor.UNKNOWN)
        {
            thing.setCellColor(x1,y1,c2 == CellColor.BLACK ? CellColor.WHITE : CellColor.BLACK);
            return LogicStatus.LOGICED;
        }

        thing.setCellColor(x2,y2,c1 == CellColor.BLACK ? CellColor.WHITE : CellColor.BLACK);
        return LogicStatus.LOGICED;
    }
}
