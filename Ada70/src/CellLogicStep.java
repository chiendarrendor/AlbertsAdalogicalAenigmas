import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.util.List;

public class CellLogicStep implements LogicStep<Board> {
    int x;
    int y;
    public CellLogicStep(int x, int y) { this.x = x; this.y = y; }

    @Override public LogicStatus apply(Board thing) {
        if (thing.getCell(x,y).isIllegal()) return LogicStatus.CONTRADICTION;

        if (thing.getCell(x,y).getPresenceType() != PresenceType.REQUIRED) return LogicStatus.STYMIED;
        if (thing.getCell(x,y).getPathType() == PathType.TERMINAL) return LogicStatus.STYMIED;

        //System.out.println("Required Logic in play for " + x + " " + y);

        List<Jump> currentJumps = thing.getBackReferences(x,y).stillValidJumps(thing);
        if (currentJumps.size() == 0) return LogicStatus.CONTRADICTION;
        if (currentJumps.size() > 1) return LogicStatus.STYMIED;

        Jump theJump = currentJumps.get(0);
        JumpSet js = thing.getJumpSet(theJump.base.x,theJump.base.y);

        if (!js.contains(theJump)) throw new RuntimeException("But you just said it did!");
        if (!theJump.isLegal(thing)) return LogicStatus.CONTRADICTION;
        js.set(theJump);
        theJump.place(thing);
        return LogicStatus.LOGICED;
    }
}
