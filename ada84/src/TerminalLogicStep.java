import grid.logic.LogicStatus;

public class TerminalLogicStep extends EdgeSensitiveLogicStep {
    public TerminalLogicStep(int x, int y) { super(x,y); }

    //       paths
    // walls
    //          0   1   2   3   4
    //      0       c   a   a   a
    //      1       c   a   a   X
    //      2       c   a   X   X
    //      3   d   S   X   X   X
    //      4   b   X   X   X   X
    //
    // if we are a terminal cell, we _must_ be on path, but exactly one edge can be path
    // a = too many paths
    // b = surrounded by walls
    // S = 1 path, 3 walls (no unknowns) ... legal
    // c = 1 path, any unknowns...all other adjacents must be walls
    // d = 3 walls, 1 unknown ... unknown must be path


    @Override public LogicStatus apply(Board thing) {
        calculate(thing);
        if (pathcount > 1) return LogicStatus.CONTRADICTION; // a
        if (wallcount == 4) return LogicStatus.CONTRADICTION; // b
        if (unkowns.size() == 0) return LogicStatus.STYMIED; // S
        if (pathcount == 1) {  // c
            unkowns.stream().forEach(p->thing.setCell(p.x,p.y,CellType.WALL));
            return LogicStatus.LOGICED;
        }
        if (wallcount == 3) {
            unkowns.stream().forEach(p->thing.setCell(p.x,p.y,CellType.PATH));
            return LogicStatus.LOGICED;
        }

        return LogicStatus.STYMIED;
    }
}
