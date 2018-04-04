import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.util.HashMap;
import java.util.Map;

public class WallsCloseInLogicStep extends CellLogicStep {
    public WallsCloseInLogicStep(int x, int y) {
        super(x,y);
    }

    private Map<Direction,EdgeInfo> walls = new HashMap<>();
    private Map<Direction,EdgeInfo> notWalls = new HashMap<>();


    public LogicStatus apply(Board thing) {
        if (thing.isCellComplete(x,y)) return LogicStatus.STYMIED;

        divvy(thing,(d,ei)->ei.isWall(),walls);

        if (walls.size() < 3) return LogicStatus.STYMIED;
        if (walls.size() == 4) {
            if (thing.isCellComplete(x,y)) return LogicStatus.STYMIED;
            thing.setCellComplete(x,y);
            return LogicStatus.LOGICED;
        }

        divvy(thing,(d,ei)->!ei.isWall(),notWalls);
        // we know there's only one of these.
        Map.Entry<Direction,EdgeInfo> ment = notWalls.entrySet().iterator().next();
        Direction d = ment.getKey();
        EdgeInfo ei = ment.getValue();

        if (ei.isUsed()) return LogicStatus.CONTRADICTION;
        ei.clear();
        thing.setCellComplete(x,y);
        return LogicStatus.LOGICED;
    }
}
