import grid.logic.flatten.FlattenLogicer;

/**
 * Created by chien on 11/6/2017.
 */
public class Solver extends FlattenLogicer<Board>
{
    public Solver(Board b)
    {
        addLogicStep(new BlackConnectivityLogicStep());
        b.forEachCell((x,y) -> {

            Clues.VInt vi = b.clues.clues[x][y];
            if (vi != null)
            {
                addLogicStep(new ClueLogicStep(x,y));
            }


            if (x == b.getWidth() - 1) return;
            if (y == b.getHeight() - 1) return;
            addLogicStep(new QuadBlackLogicStep(x,y));
        });
    }
}
