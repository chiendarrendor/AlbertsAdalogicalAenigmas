import grid.logic.LogicStatus;
import grid.logic.LogicStep;

public class CrateLogicStep implements LogicStep<CrateBoard> {
    int x;
    int y;

    public CrateLogicStep(int x, int y) { this.x = x; this.y = y; }

    @Override public LogicStatus apply(CrateBoard thing) {
        CrateShiftCellHolder csch = thing.cells.getCell(x,y);
        int icount  = csch.initialSize();
        if (icount == 0) return LogicStatus.CONTRADICTION;
        if (icount > 1) return LogicStatus.STYMIED;
        CrateShift theShift = csch.getUniqueInitial();
        if (thing.isLocked(theShift)) return LogicStatus.STYMIED;
        return thing.checkingSet(theShift) ? LogicStatus.LOGICED : LogicStatus.CONTRADICTION;
    }
}
