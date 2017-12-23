import grid.logic.LogicStatus;

/**
 * Created by chien on 12/16/2017.
 */
public class OminoesOfShapeLogicStep implements grid.logic.LogicStep<Board>
{
    public LogicStatus apply(Board thing)
    {
        thing.ob.clearDelCount();
        for(OminoBoard.OminoPlaceSet ops : thing.ob.ominosets.values())
        {
            if(ops.count > ops.ominoes.size()) return LogicStatus.CONTRADICTION;
            if(ops.count < ops.ominoes.size()) continue;

            thing.ob.safeIterateOminoPlaceSet(ops.ominoes, op -> {
                op.placeDown();
            });
        }

        return thing.ob.getDelCount() > 0 ? LogicStatus.LOGICED : LogicStatus.STYMIED;
    }
}
