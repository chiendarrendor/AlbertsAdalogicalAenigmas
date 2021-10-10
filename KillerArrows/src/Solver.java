import grid.logic.flatten.FlattenLogicer;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Solver  extends FlattenLogicer<Board> {
    public Solver(Board b) {
        for (Arrow arrow : b.getArrowList()) addLogicStep(new ArrowLogicStep(arrow));
        for (Cage cage : b.getCageList()) addLogicStep(new CageLogicStep(cage));

        for (int x = 0 ; x < b.getWidth() ; ++x) {
            List<Point> column = new ArrayList<>();
            for (int y = 0 ; y < b.getHeight() ; ++y) column.add(new Point(x,y));
            addLogicStep(new NumericSingleLogicStep(column));
        }

        for (int y = 0 ; y < b.getHeight() ; ++y) {
            List<Point> row = new ArrayList<>();
            for (int x = 0 ; x < b.getWidth() ; ++x) row.add(new Point(x,y));
            addLogicStep(new NumericSingleLogicStep(row));
        }

        for (int x = 0 ; x < 3 ; ++ x) {
            for (int y = 0 ; y < 3 ; ++y) {
                List<Point> nonant = new ArrayList<>();
                for (int ix = x*3 ; ix < x*3+3 ; ++ix) {
                    for (int iy = y * 3; iy < y * 3 + 3; ++iy) {
                        nonant.add(new Point(ix,iy));
                    }
                }
                addLogicStep(new NumericSingleLogicStep(nonant));
            }
        }


    }
}
