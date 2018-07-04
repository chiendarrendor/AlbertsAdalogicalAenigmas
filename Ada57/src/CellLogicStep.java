import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.List;

public class CellLogicStep implements LogicStep<Board> {
    int x;
    int y;
    public CellLogicStep(int x, int y) { this.x = x; this.y = y; }

    private boolean hasClue(Board thing, Direction d) {
        List<Point> path = Tracer.trace(thing,x,y,d,true,true);
        if (path.size() == 0) return false;
        Point lp = path.get(path.size()-1);
        return thing.getCell(lp.x,lp.y) == CellState.NUMBER;
    }


    @Override public LogicStatus apply(Board thing) {
        boolean canBeVertical = hasClue(thing,Direction.NORTH) || hasClue(thing,Direction.SOUTH);
        boolean canBeHorizontal = hasClue(thing,Direction.EAST) || hasClue(thing,Direction.WEST);
        CellState cs = thing.getCell(x,y);

        if (cs == CellState.VERTICAL) return canBeVertical ? LogicStatus.STYMIED : LogicStatus.CONTRADICTION;
        if (cs == CellState.HORIZONTAL) return canBeHorizontal ? LogicStatus.STYMIED : LogicStatus.CONTRADICTION;

        // if we get here, cs is UNKNOWN
        if (canBeHorizontal && canBeVertical) return LogicStatus.STYMIED;
        if (!canBeHorizontal && !canBeVertical) return LogicStatus.CONTRADICTION;

        if (canBeHorizontal) thing.setCell(x,y,CellState.HORIZONTAL);
        if (canBeVertical) thing.setCell(x,y,CellState.VERTICAL);
        return LogicStatus.LOGICED;
    }
}
