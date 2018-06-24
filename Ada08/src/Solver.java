import grid.logic.flatten.FlattenLogicer;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b) {
        b.forEachCell((x,y)-> {
            addLogicStep(new CellLogicStep(x,y));

            if (b.getCell(x,y) == CellState.ARROW) {
                ArrowInfo ai = b.getArrowInfo(x,y);
                List<Point> arrowtargets = new ArrayList<>();
                Point basep = new Point(x,y);
                int index = 1;
                while(true) {
                    Point np = ai.d.delta(basep,index);
                    if (b.inBounds(np.x,np.y)) arrowtargets.add(np);
                    else break;
                    ++index;
                }
                addLogicStep(new ArrowLogicStep(ai.size,arrowtargets));

                return;
            }

            // we get here if it is _not_ an arrow
            if (b.inBounds(x+1,y) && b.getCell(x+1,y) != CellState.ARROW)
                addLogicStep(new PairLogicStep(x,y,x+1,y));
            if (b.inBounds(x,y+1) && b.getCell(x,y+1) != CellState.ARROW)
                addLogicStep(new PairLogicStep(x,y,x,y+1));
        });

        addLogicStep(new LoopLogicStep());
        addLogicStep(new ConnectivityLogicStep());


        // one loop

    }
}
