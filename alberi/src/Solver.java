import grid.logic.flatten.FlattenLogicer;

import java.awt.*;
import java.util.Vector;

/**
 * Created by chien on 5/27/2017.
 */
public class Solver extends FlattenLogicer<Board>
{
    public Solver(Board start)
    {
        for (Vector<Point> hp : start.hlines.values()) addLogicStep(new CellCollectionLogicStep(hp,start.count));
        for (Vector<Point> vp : start.vlines.values()) addLogicStep(new CellCollectionLogicStep(vp,start.count));
        for (Vector<Point> rp : start.regions.values()) addLogicStep(new CellCollectionLogicStep(rp,start.count));

        addLogicStep(new NoAdjacentLogicStep());
    }
}
