import grid.logic.LogicStatus;
import grid.logic.LogicStep;

public class CellLogicStep implements LogicStep<Board> {
    int x;
    int y;
    public CellLogicStep(int x, int y) { this.x = x; this.y = y; }

    @Override public LogicStatus apply(Board thing) {
        DestinationSet ds = thing.getCellDestinations(x,y);
        if (ds.size() == 0) return LogicStatus.CONTRADICTION;
        if (ds.size() == 1) {
            PossibleDestination pd = ds.getOne();
            return thing.setDestination(pd) ? LogicStatus.LOGICED : LogicStatus.STYMIED;
        }

        return LogicStatus.STYMIED;
    }
}
