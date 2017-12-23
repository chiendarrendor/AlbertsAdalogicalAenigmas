import grid.logic.flatten.FlattenLogicer;

/**
 * Created by chien on 12/16/2017.
 */
public class Solver extends FlattenLogicer<Board>
{

    public Solver()
    {
        addLogicStep(new OneOminoInCellLogicStep());
        addLogicStep(new OminoesOfShapeLogicStep());
        addLogicStep(new CommonOminoElementsLogicStep());
        addLogicStep(new ConnectivityLogicStep());
        addLogicStep(new RequiredColorLogicStep());
    }
}
