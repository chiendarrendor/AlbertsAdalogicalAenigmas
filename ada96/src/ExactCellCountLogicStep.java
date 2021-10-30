import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class ExactCellCountLogicStep implements LogicStep<Board> {
    List<Point> cells = null;
    int desiredKnightCount = -1;
    
    public void init(List<Point> cells, int desiredKnightCount) { this.cells = cells; this.desiredKnightCount = desiredKnightCount; }
    
    @Override public LogicStatus apply(Board thing) {

        int knightcount = 0;
        int noknightcount = 0;
        List<Point> unknowns = new ArrayList<>();

        for (Point p : cells) {
            CellState cs = thing.getCell(p.x,p.y);
            switch(cs) {
                case UNKNOWN: unknowns.add(p);  break;
                case CANT_STOP_HERE: ++noknightcount;   break;
                case MUST_HAVE_KNIGHT: ++knightcount; break;
                case POSITION_INITIAL: unknowns.add(p);  break;
                case POSITION_INTERMEDIATE: ++noknightcount; break;
                case POSITION_FINAL: ++knightcount;  break;
            }
        }

        if (knightcount > desiredKnightCount) return LogicStatus.CONTRADICTION;
        if (knightcount + unknowns.size() < desiredKnightCount) return LogicStatus.CONTRADICTION;
        if (unknowns.size() == 0) return LogicStatus.STYMIED;

        if (knightcount == desiredKnightCount) {
            for (Point p : unknowns) {
                if (thing.getCell(p.x,p.y) == CellState.UNKNOWN) {
                    thing.setCell(p.x,p.y,CellState.CANT_STOP_HERE);
                } else { // two ways for cell to be uknown: UNKNOWN and POSITION_INITIAL ... POSITION_INITIAL requires some extra processing
                    thing.processInitialCell(p,CellState.POSITION_INTERMEDIATE); // require that the knight have to move from its initial position
                }
            }
            return LogicStatus.LOGICED;
        }

        if (knightcount + unknowns.size() == desiredKnightCount) {
            for (Point p : unknowns) {
                if (thing.getCell(p.x,p.y) == CellState.UNKNOWN) {
                    thing.setCell(p.x,p.y,CellState.MUST_HAVE_KNIGHT);
                } else {
                    thing.processInitialCell(p,CellState.POSITION_FINAL);
                }
            }
            return LogicStatus.LOGICED;
        }


        return LogicStatus.STYMIED;
    }
}
