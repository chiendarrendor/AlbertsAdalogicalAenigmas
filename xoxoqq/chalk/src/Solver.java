import grid.logic.flatten.FlattenLogicer;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b) {
        b.forEachCell((x,y)-> {
            if (b.getRegion(x,y) != null) addLogicStep(new CellLogicStep(x,y));
        });
        for(Region r : b.getRegions()) {
            addLogicStep(new RegionLogicStep(r.getId()));
        }
    }

    private List<Point> makeLine(int x, int y, Direction d, int count) {
        Point p = new Point(x,y);
        List<Point> result = new ArrayList<>();

        for (int i = 0 ; i < count ; ++i) result.add(d.delta(p,i));
        return result;
    }
}
