import grid.logic.flatten.FlattenLogicer;

/**
 * Created by chien on 8/17/2017.
 */
public class Solver extends FlattenLogicer<LogicBoard>
{
    public Solver(Board b)
    {
        addLogicStep(new ConnectivityLogicStep());

        // iterating over corner space, to find numbered corners
        for (int x = 0 ; x <= b.getWidth() ; ++x)
        {
            for (int y = 0 ; y <= b.getHeight() ; ++y)
            {
                if (b.getCorner(x,y) == -1) continue;
                addLogicStep(new CornerLogicStep(b.getCorner(x,y),b.getAdjacents(x,y)));
            }
        }



    }
}
