import grid.logic.LogicStatus;
import grid.logic.LogicStep;

public class EmptyCellLogicStep implements LogicStep<CrateBoard> {
    int x;
    int y;
    public EmptyCellLogicStep(int x, int y) { this.x = x; this.y = y; }

    @Override public LogicStatus apply(CrateBoard thing) {
        CrateShiftCellHolder csch = thing.cells.getCell(x,y);
        if (csch.terminalSize() > 0) return LogicStatus.STYMIED;
        switch (thing.termstat(x,y)) {
            case UNKNOWN:
                thing.settermstat(x,y,TerminalState.MUSTBEEMPTY);
                return LogicStatus.LOGICED;
            case MUSTBEEMPTY:
                return LogicStatus.STYMIED;
            case MUSTHAVEBOX:
                return LogicStatus.CONTRADICTION;
            default:
                throw new RuntimeException("Shouldn't get here?");
        }
    }
}
