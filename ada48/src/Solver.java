import grid.logic.flatten.FlattenLogicer;

/**
 * Created by chien on 10/14/2017.
 */
public class Solver extends FlattenLogicer<Board>
{
    public Solver(Board b)
    {
        b.forEachCell((x,y)->{
            addLogicStep(new CellLogicStep(x,y));
            return true;
        });

        b.forEachCell((x,y)->{
            if (x == b.getWidth()-1) return true;
            if (y == b.getHeight() - 1) return true;
            addLogicStep(new QuadLogicStep(x,y));
            return true;
        });

        for(char rid : b.regions.keySet()) { addLogicStep(new RegionLogicStep(rid)); }

    }
}
