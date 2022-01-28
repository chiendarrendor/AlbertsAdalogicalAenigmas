import grid.logic.flatten.FlattenLogicer;

import java.awt.Point;
import java.util.Set;

public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b) {
        for (int y = 0 ; y < b.getHeight() ; ++y) {
            for (int x = 0 ; x < b.getWidth() ; ++x) {
                if (b.isQuadInBounds(x,y)) addLogicStep(new QuadPartialLogicStep(x,y));
                if (b.hasNumber(x,y)) addLogicStep(new NumericClueLogicStep(b,x,y,b.getNumber(x,y)));
            }
        }
        for (int rid : b.getRegionIds()) {
            Set<Point> cells = b.getRegionCells(rid);
            if (cells.size() < 2) continue;
            addLogicStep(new SolidRegionLogicStep(cells));
        }
        addLogicStep(new ConnectedShadedCellsLogicStep());
    }
}
