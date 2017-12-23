import grid.logic.LogicStep;
import grid.logic.simple.Logicer;

/**
 * Created by chien on 5/19/2017.
 */
public class Solver extends Logicer<Board>
{
    LogicStep<Board> adj = new NoAdjacentLogicStep();
    LogicStep<Board> conn = new ConnectivityLogicStep();
    LogicStep<Board> clue = new ClueLogicStep();


    public Solver()
    {
        addLogicStep(adj);
        addLogicStep(conn);
        addLogicStep(clue);
    }

}
