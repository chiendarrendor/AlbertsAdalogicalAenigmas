import grid.logic.flatten.FlattenLogicer;

/**
 * Created by chien on 6/16/2017.
 */
public class Solver extends FlattenLogicer<Board>
{
    public Solver(Board b)
    {
        for (int i = 0 ; i < b.getNumClues() ; ++i)
        {
            addLogicStep(new ClueLogicStep(i));
        }
    }
}
