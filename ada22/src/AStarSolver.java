import grid.logic.astar.AStarLogicer;
import grid.logic.LogicStep;

/**
 * Created by chien on 5/20/2017.
 */
public class AStarSolver extends AStarLogicer<Board>
{
    LogicStep<Board> adj = new NoAdjacentLogicStep();
    LogicStep<Board> conn = new ConnectivityLogicStep();
    LogicStep<Board> clue = new ClueLogicStep();


    public AStarSolver()
    {
        addLogicStep(adj);
        addLogicStep(conn);
        addLogicStep(clue);
    }
}
