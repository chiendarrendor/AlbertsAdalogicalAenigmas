import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.util.ArrayList;
import java.util.List;

public class TerminalStateLogicStep implements LogicStep<CrateBoard> {
    int x;
    int y;
    public TerminalStateLogicStep(int x, int y) { this.x = x; this.y = y; }

    @Override public LogicStatus apply(CrateBoard thing) {
        CrateShiftCellHolder csch = thing.cells.getCell(x,y);

        switch(thing.terminalstatus.getCell(x,y)) {
            case UNKNOWN: return LogicStatus.STYMIED;
            case MUSTHAVEBOX:
                if (csch.terminalSize() == 0) return LogicStatus.CONTRADICTION;
                if (csch.terminalSize() > 1) return LogicStatus.STYMIED;
                if (thing.isLocked(csch.getUniqueTerminal())) return LogicStatus.STYMIED;
                return thing.checkingSet(csch.getUniqueTerminal()) ? LogicStatus.LOGICED : LogicStatus.CONTRADICTION;
            case MUSTBEEMPTY:
                LogicStatus result = LogicStatus.STYMIED;
                if (thing.cells.getCell(x,y).terminalSize() == 1 && thing.isLocked(csch.getUniqueTerminal())) return LogicStatus.CONTRADICTION;
                List<CrateShift> doomed = new ArrayList<>();
                doomed.addAll(csch.getTerminals());
                for (CrateShift cs : doomed) {
                    thing.remove(cs);
                    result = LogicStatus.LOGICED;
                }
                return result;
            default: throw new RuntimeException("what did we miss?");
        }
    }
}
