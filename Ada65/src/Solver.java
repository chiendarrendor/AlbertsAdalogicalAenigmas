import grid.lambda.CellLambda;
import grid.logic.flatten.FlattenLogicer;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.Set;

public class Solver extends FlattenLogicer<Board> {
    private static final Direction[] largers = new Direction[] { Direction.EAST,Direction.SOUTHEAST,Direction.SOUTH,Direction.SOUTHWEST};

    public Solver(Board b) {
        CellLambda.stream(b.getWidth(),b.getHeight())
            .forEach(p->{
                for (Direction d : largers) {
                    Point dp = d.delta(p,1);
                    if (b.onBoard(dp.x,dp.y)) addLogicStep(new DifferentLogicStep(p,dp));
                }
            });
        for (Set<Point> region : b.getRegionPoints() ) addLogicStep(new RegionLogicStep(region));
    }
}
