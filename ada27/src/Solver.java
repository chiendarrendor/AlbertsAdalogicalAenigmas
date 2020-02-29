import grid.logic.flatten.FlattenLogicer;

import java.awt.Point;
import java.util.Set;

public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b) {
        b.forEachCell((x,y)-> {
            Point p = new Point(x,y);

            if (p.equals(b.getEntrance())) addLogicStep(new TerminalLogicStep(p));
            else if (p.equals(b.getExit())) addLogicStep(new TerminalLogicStep(p));
            else if (b.hasIce(x,y)) addLogicStep(new IceLogicStep(p));
            else addLogicStep(new NormalCellLogicStep(p));
        });

        for (Set<Point> iceregion : b.getIceRegions()) {
            addLogicStep(new IceRegionLogicStep(iceregion));
        }

        addLogicStep(new PathLogicStep());

    }
}
