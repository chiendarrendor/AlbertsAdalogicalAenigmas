import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.logic.flatten.FlattenLogicer;

/**
 * Created by chien on 9/4/2017.
 */
public class Solver extends FlattenLogicer<LogicBoard>
{
    private LogicStep<LogicBoard> pathLogicStep;


    public Solver(LogicBoard b)
    {
        b.forEachCell( (x,y) -> {
            addLogicStep(new CellLogicStep(x,y,b.getSolverID() == 74));
            if (b.getSolverID() == 74 && b.getClue(x,y) == '.') addLogicStep(new NoQuadLogicStep(x,y));
            if (b.getSolverID() == 74 && b.hasNumericClue(x,y)) addLogicStep(new NumericClueLogicStep(x,y,b.getNumericClue(x,y)));
            return true;
        });


        pathLogicStep = new PathLogicStep();
    }

    // special behavior here.    Want to make sure that all individual cells
    // are correct and as complete as possible before applying the pathing logic.
    @Override
    public RecursionStatus recursiveApplyLogic(LogicBoard thing)
    {
        RecursionStatus rs = super.recursiveApplyLogic(thing);
        if (rs == RecursionStatus.DEAD) return RecursionStatus.DEAD;

        LogicStatus ls = pathLogicStep.apply(thing);
        if (ls == LogicStatus.CONTRADICTION) return RecursionStatus.DEAD;
        if (thing.isComplete()) return RecursionStatus.DONE;

        return RecursionStatus.GO;
    }


}
