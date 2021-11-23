import grid.logic.flatten.FlattenLogicer;
import grid.solverrecipes.singleloopflatten.CellPathLogicStep;
import grid.solverrecipes.singleloopflatten.SinglePathLogicStep;

public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b) {
        CellPathLogicStep.generateLogicSteps(this,b,true);

        addLogicStep(new SinglePathLogicStep<Board>());

        for (char rid : b.getRegionIds()) {
            if (!b.hasRegionSize(rid)) continue;
            addLogicStep(new RegionLogicStep(rid, b,b.getRegionCells(rid),b.getRegionSize(rid)));
        }
    }
}
