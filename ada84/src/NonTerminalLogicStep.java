import grid.logic.LogicStatus;
import grid.logic.LogicStep;

public class NonTerminalLogicStep extends EdgeSensitiveLogicStep {
    public NonTerminalLogicStep(int x, int y) { super(x,y); }


    //
    // requirements:
    // this cell can be either a path or a wall.  A wall have no particular requirements of its edges
    // a path cannot be a terminal path


    // behavior for I am unknown type:
    //       paths
    // walls
    //          0   1   2   3   4
    //      0
    //      1                   X
    //      2               X   X
    //      3   a   a   X   X   X
    //      4   a   X   X   X   X
    // X = impossible
    // a = 3 or more walls, meaning I cannot be a non-terminal/connecting path, which means I must be a wall
    // behavior for I am PATH type
    //       paths
    // walls
    //          0   1   2   3   4
    //      0                   S
    //      1               S   X
    //      2   b   b   S   X   X
    //      3   a   a   X   X   X
    //      4   a   X   X   X   X
    //  X = impossible
    //  a = more than 2 walls, meaning I would have to be a terminal/non-connecting path, so CONTRADICTION
    //  S = legal combinations of paths/walls with no unknowns
    //  b = 2 walls, all other edges must be paths




    @Override public LogicStatus apply(Board thing) {
        if (thing.getCell(x,y) == CellType.WALL) return LogicStatus.STYMIED;
        calculate(thing);
        if (thing.getCell(x,y) == CellType.UNKNOWN) {
            if (wallcount > 2) {
                thing.setCell(x,y,CellType.WALL);
                return LogicStatus.LOGICED;
            }
            return LogicStatus.STYMIED;
        }
        // if we get here, we are known to be path

        if (wallcount > 2) return LogicStatus.CONTRADICTION; // a
        if (unkowns.size() == 0) return LogicStatus.STYMIED; // S
        if (wallcount == 2) {
            unkowns.stream().forEach(p->thing.setCell(p.x,p.y,CellType.PATH));
            return LogicStatus.LOGICED;
        }

        return LogicStatus.STYMIED;
    }
}
