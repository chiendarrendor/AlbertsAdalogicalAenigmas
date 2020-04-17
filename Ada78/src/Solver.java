import grid.logic.flatten.FlattenLogicer;
import grid.puzzlebits.Direction;

import java.awt.Point;

public class Solver extends FlattenLogicer<Board> {


    private void checkAddCrossRegionLogicStep(Board b, int x, int y, Direction d) {
        Point op = d.delta(x,y,1);
        if (!b.onBoard(op.x,op.y)) return;
        char r1 = b.getRegionId(x,y);
        char r2 = b.getRegionId(op);
        if (r1 == r2) return;
        addLogicStep(new SameLabelCrossRegionAdjacentLogicStep(b,x,y,op));
    }

    public Solver(Board b) {
        for (char rid : b.getRegionIds()) {
            if (b.getRegionLabel(rid) == -1) {
                // for each region where a label is not already known: # of labeled cells must be at least 1
                addLogicStep(new EmptyRegionLabelCountLogicStep(rid));
            } else {
                // for each region where a label is already known:  # of labeled cells must be exactly value of label
                addLogicStep(new PreLabeledRegionLabelCountLogicStep(rid));
            }

        }




        b.forEachCell((x,y)-> {
            // for each pair of adjacent cells belonging to different regions, if those regions have the same label,
            //    at least one of the two cells must be a barrier
            checkAddCrossRegionLogicStep(b,x,y,Direction.EAST);
            checkAddCrossRegionLogicStep(b,x,y, Direction.SOUTH);


            // for each 2x2 set of cells, not all of them can be labels.
            if (x == b.getWidth() - 1) return;
            if (y == b.getHeight() - 1) return;
            addLogicStep(new QuadLogicStep(x,y));
        });


        // all labels must form one connected set.
        addLogicStep(new LabelConnectivityLogicStep());



    }

}
