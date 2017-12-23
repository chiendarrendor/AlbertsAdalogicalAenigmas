import grid.logic.flatten.FlattenLogicer;

import java.util.Collection;

/**
 * Created by chien on 8/13/2017.
 */
public class Solver extends FlattenLogicer<LogicBoard>
{
    public Solver(LogicBoard b)
    {
        for (int x = 0 ; x < b.getWidth() ; ++x)
        {
            for (int y = 0 ; y < b.getHeight() ; ++y)
            {
                if (b.hasArrow(x,y))
                {
                    addLogicStep(new ArrowLogicStep(b,x,y));
                }
                else
                {
                    addLogicStep(new CellLogicStep(b, x, y));
                }
            }
        }

        for (Character rid : b.getRegionIds())
        {
            addLogicStep(new RegionLogicStep(b,rid));
        }
    }
}
