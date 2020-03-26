import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class ClueLogicStep implements LogicStep<CrateBoard> {
    int x;
    int y;
    int size;
    Direction d;
    public ClueLogicStep(int x, int y, int clueSize, Direction clueDirection) { this.x = x; this.y = y; size = clueSize; d = clueDirection; }

    @Override public LogicStatus apply(CrateBoard thing) {
        // two calculations:
        // a) clue state:  known hidden, known visible, or unknown
        CrateShiftCellHolder csch = thing.cells.getCell(x,y);
        TerminalState thists = thing.terminalstatus.getCell(x,y);
        // known hidden.
        if (csch.terminalSize() == 1 && thing.isLocked(csch.getUniqueTerminal())) return LogicStatus.STYMIED;
        if (thists == TerminalState.MUSTHAVEBOX) return LogicStatus.STYMIED;

        // a) if not known visible, (and not known hidden above) then we don't know.
        boolean knownVisible = csch.terminalSize() == 0 || thists == TerminalState.MUSTBEEMPTY;

        // b) min and max possible final positions
        int emptycount = 0;
        List<Point> unknowns = new ArrayList<>();
        int boxcount = 0;


        for (int i = 1 ; ; ++i) {
            Point np = d.delta(x,y,i);
            if (!thing.onBoard(np)) break;

            CrateShiftCellHolder nph = thing.cells.getCell(np.x,np.y);
            TerminalState ts = thing.terminalstatus.getCell(np.x,np.y);

            if (nph.terminalSize() == 0 || ts == TerminalState.MUSTBEEMPTY) {
                ++emptycount;
                continue;
            }

            if (ts == TerminalState.MUSTHAVEBOX || (nph.terminalSize() == 1 && thing.isLocked(nph.getUniqueTerminal()))) {
                ++boxcount;
                continue;
            }

            // if we get here, we have at least one box, but not a single locked one....
            unknowns.add(np);
        }

        boolean inbounds = boxcount <= size && size <= boxcount + unknowns.size();

        if (knownVisible && !inbounds) return LogicStatus.CONTRADICTION;
        if (inbounds || !inbounds) return LogicStatus.STYMIED;

        if (!inbounds) {
            thing.terminalstatus.setCell(x,y,TerminalState.MUSTHAVEBOX);
            return LogicStatus.LOGICED;
        }

        // if we get here, we are in bounds.
        if (!knownVisible) return LogicStatus.STYMIED;

        // if we get here, we are in bounds, and have to be in bounds.
        // let's see if we can do anything useful.
        if (unknowns.size() == 0) return LogicStatus.STYMIED;

        if (boxcount == size) {
            unknowns.stream().forEach(p->thing.terminalstatus.setCell(p.x,p.y,TerminalState.MUSTBEEMPTY));
            return LogicStatus.LOGICED;
        }

        if (boxcount + unknowns.size() == size) {
            unknowns.stream().forEach(p->thing.terminalstatus.setCell(p.x,p.y,TerminalState.MUSTHAVEBOX));
            return LogicStatus.LOGICED;
        }





        return LogicStatus.STYMIED;
    }
}
