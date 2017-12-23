import grid.logic.LogicStatus;

import java.awt.*;

/**
 * Created by chien on 12/16/2017.
 */
public class OneOminoInCellLogicStep implements grid.logic.LogicStep<Board>
{
    public Point badPoint = null;

    public LogicStatus apply(Board thing)
    {
        thing.ob.clearDelCount();
        boolean status = thing.terminatingForEachCell((x,y) ->
        {
            OminoBoard.OminoCell oc = thing.ob.cells[x][y];
            if (oc.thePlace != null) return true;
            if (oc.requiredColor != CellColor.BLACK) return true;
            if (oc.onplaces.size() == 0) { badPoint = new Point(x,y); return false; }
            if (oc.onplaces.size() > 1) return true;

            oc.onplaces.iterator().next().placeDown();
            return true;
        });

        if (status == false) return LogicStatus.CONTRADICTION;
        return thing.ob.getDelCount() > 0 ? LogicStatus.LOGICED : LogicStatus.STYMIED;
    }

    public String toString() { return "OneOminoInCellLogicStep: " + badPoint; }
}
