import grid.logic.flatten.FlattenLogicer;
import grid.puzzlebits.Direction;

/**
 * Created by chien on 10/18/2017.
 */
public class Solver extends FlattenLogicer<Board>
{
    public Solver(Board b)
    {

        for(int y = 0 ; y < b.getHeight() ; ++y)
        {
            for (int x = 0 ; x < b.getWidth() ; ++x)
            {
                addLogicStep(new CellLogicStep(x,y));
                if (x != b.getWidth() - 1)
                {
                    addLogicStep(new AdjacentCellLogicStep(x, y, x + 1, y,Direction.EAST));
                    if (b.getRegionId(x, y) != b.getRegionId(x + 1, y))
                        addLogicStep(new AdjacentCellCrossRegionLogicStep(x, y, x + 1, y));
                }

                if (y != b.getHeight() - 1)
                {
                    addLogicStep(new AdjacentCellLogicStep(x,y,x,y+1, Direction.SOUTH));

                    if (b.getRegionId(x,y) != b.getRegionId(x,y+1))
                        addLogicStep(new AdjacentCellCrossRegionLogicStep(x,y,x,y+1));
                }

            }
        }

        for (char rid : b.getRegionIds())
        {
            addLogicStep(new RegionEntryExitLogicStep(rid));
            addLogicStep(new RegionConnectivityLogicStep(rid));
            int cc = b.getRegionExpectedCellCount(rid);
            if (cc > 0) addLogicStep(new RegionPathCountLogicStep(rid,cc));
        }

        addLogicStep(new SingleLoopLogicStep());
    }

}
