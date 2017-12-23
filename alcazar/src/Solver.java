import grid.logic.flatten.FlattenLogicer;

/**
 * Created by chien on 7/24/2017.
 */
public class Solver extends FlattenLogicer<LogicBoard>
{
    public Solver(LogicBoard b)
    {
        for (int x = 0 ; x < b.getBoard().getWidth() ; ++x)
        {
            for (int y = 0 ; y < b.getBoard().getHeight() ; ++y)
            {
                if (b.getBoard().getCI(x,y).isOptional)
                {
                    addLogicStep(new OptionalCellLogicStep(x,y,b.getBoard()));
                }
                else
                {
                    addLogicStep(new CellLogicStep(x, y, b.getBoard()));
                }
            }
        }

        addLogicStep(new OutsideLogicStep(b.getBoard()));
        addLogicStep(new PathLogicStep());
    }




}
