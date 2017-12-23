import grid.logic.flatten.FlattenLogicer;

/**
 * Created by chien on 7/1/2017.
 */
public class Solver extends FlattenLogicer<Board>
{
    public Solver(Board b)
    {
        RabbitHops rh = b.hops;

        for (RabbitHops.Rabbit rab : rh.rabbits)
        {
            addLogicStep(new RabbitLogicStep(rab));
        }

        for (char rid : b.regionids)
        {
            addLogicStep(new RegionLogicStep(b,rid));
        }


    }
}
