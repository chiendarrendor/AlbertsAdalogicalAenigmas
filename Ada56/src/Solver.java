import grid.logic.flatten.FlattenLogicer;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b) {
        addLogicStep(new NoLoopPathLogicStep());
        for (int y = 0 ; y < b.getHeight() - 1 ; ++y) {
            for (int x = 0; x < b.getWidth() - 1 ; ++x) {
                addLogicStep(new NoTwoByTwoLogicStep(x,y));
            }
        }

        Map<Character,RegionConsistencyLogicStep> consteps = new HashMap<>();
        for (int y = 0 ; y < b.getHeight() ; ++y ) {
            for (int x = 0 ; x < b.getWidth() ; ++x) {
                addLogicStep(new OnPathLogicStep(b,new Point(x,y)));
                char rid = b.getRegionId(x,y);
                if (!consteps.containsKey(rid)) {
                    RegionConsistencyLogicStep rcls = new RegionConsistencyLogicStep();
                    consteps.put(rid,rcls);
                    addLogicStep( rcls);
                }
                consteps.get(rid).addCell(new Point(x,y));
            }
        }

        addLogicStep(new AllPathConnectedLogicStep());

        for (Point p : b.getTriangleSet()) {
            addLogicStep(new TriangleNotOnPathLogicStep(p));
        }

    }
}
