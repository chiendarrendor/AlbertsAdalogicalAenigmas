import grid.logic.flatten.FlattenLogicer;

/**
 * Created by chien on 6/12/2017.
 */
public class Solver  extends FlattenLogicer<Board>
{
    public Solver(Board bd)
    {
        BoardCore b = bd.getBoardCore();
        for (int x = 0 ; x < b.getWidth() ; ++ x)
        {
            for (int y = 0 ; y < b.getHeight() ; ++y)
            {
                addLogicStep(new IslandLogicStep(x,y,b.getCount(x,y)));
            }
        }
        addLogicStep(new ConnectivityLogicStep());

    }
}
