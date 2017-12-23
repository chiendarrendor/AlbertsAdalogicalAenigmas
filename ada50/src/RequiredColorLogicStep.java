import grid.logic.LogicStatus;

import java.util.Set;

/**
 * Created by chien on 12/16/2017.
 */
public class RequiredColorLogicStep implements grid.logic.LogicStep<Board>
{
    public LogicStatus apply(Board thing)
    {
        thing.ob.clearDelCount();
        boolean status = thing.terminatingForEachCell((x,y) -> {
            OminoBoard.OminoCell oc = thing.ob.cells[x][y];
            if (oc.thePlace != null) return true;
            if (oc.requiredColor == CellColor.BLACK && oc.onplaces.size() == 0) return false;

            if (oc.requiredColor == CellColor.BLACK)
            {
                OminoBoard.safeIterateOminoPlaceSet(oc.offplaces,a->a.removeFromBoard());
            }

            if (oc.requiredColor == CellColor.WHITE)
            {
                OminoBoard.safeIterateOminoPlaceSet(oc.onplaces,a->a.removeFromBoard());
            }

            return true;
        });


        if (status == false) return LogicStatus.CONTRADICTION;
        return thing.ob.getDelCount() > 0 ? LogicStatus.LOGICED : LogicStatus.STYMIED;
    }
}
