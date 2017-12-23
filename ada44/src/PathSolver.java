import grid.logic.flatten.FlattenLogicer;

/**
 * Created by chien on 6/12/2017.
 */
public class PathSolver extends FlattenLogicer<Board>
{
    public PathSolver(Board bd)
    {
        BoardCore b = bd.getBoardCore();
        for (int x = 0 ; x < b.getWidth() ; ++ x)
        {
            for (int y = 0 ; y < b.getHeight() ; ++y)
            {
                if (b.getLetter(x,y).equals(".")) continue;
                int obj = 2;
                if (x == b.getStart().x && y == b.getStart().y) obj = 1;
                if (x == b.getEnd().x && y == b.getEnd().y) obj = 1;

                addLogicStep(new IslandLogicStep(x,y,obj));
            }
        }
        addLogicStep(new ConnectivityLogicStep(true));
    }
}
