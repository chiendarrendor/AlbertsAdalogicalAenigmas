import grid.logic.flatten.FlattenLogicer;

import java.awt.Point;
import java.util.List;

public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b) {
        for (int boxid = 0 ; boxid < b.getBoxCount() ; ++boxid) {
            for (int lineid = 0 ; lineid < b.getBoxSize() ; ++lineid) {
                List<Point> boxrow = b.getBoxRow(boxid,lineid);
                List<Point> boxcol = b.getBoxColumn(boxid,lineid);

                addLogicStep(new NoDuplicatesLogicStep(b.getMinNumber(),b.getMaxNumber(),boxrow));
                addLogicStep(new NoDuplicatesLogicStep(b.getMinNumber(),b.getMaxNumber(),boxcol));
                addLogicStep(new HillValleyLogicStep(boxrow));
                addLogicStep(new HillValleyLogicStep(boxcol));
            }
        }
    }
}
