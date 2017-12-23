/**
 * Created by chien on 5/6/2017.
 */
public class Solver extends Logicer<Board>
{
    public Solver(Board b)
    {
        addLogicStep(new BrickLogicStep());
        addLogicStep(new RegionLogicStep());
        Solve(b);
    }
}
