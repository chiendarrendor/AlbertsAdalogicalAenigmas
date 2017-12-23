import grid.logic.flatten.FlattenLogicer;
import grid.logic.LogicStep;

/**
 * Created by chien on 5/20/2017.
 */
public class FlattenSolver extends FlattenLogicer<Board>
{
    LogicStep<Board> adj = new NoAdjacentLogicStep();
    LogicStep<Board> conn = new ConnectivityLogicStep();
    LogicStep<Board> clue = new ClueLogicStep();


    public FlattenSolver()
    {
        addLogicStep(adj);
        addLogicStep(conn);
        addLogicStep(clue);
    }
}
