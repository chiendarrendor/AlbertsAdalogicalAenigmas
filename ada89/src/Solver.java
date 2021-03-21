import grid.logic.flatten.FlattenLogicer;
import grid.solverrecipes.singleloopflatten.CellPathLogicStep;
import grid.solverrecipes.singleloopflatten.SinglePathLogicStep;

public class Solver extends FlattenLogicer<Board> {
    public Solver() {}
    public Solver(IntermediateCallback<Board> cb,int maxdepth) { super(cb,maxdepth); }


    public void init(Board b) {
        CellPathLogicStep.generateLogicSteps(this,b,true);
        addLogicStep(new SinglePathLogicStep<Board>());

        for (char c : b.getRegionIds()) {
            addLogicStep(new RegionConnectionLogicStep(b.getRegionInfo(c)));
            if (b.getRegionInfo(c).triarticulation != null)
                addLogicStep(new RegionTriArticulationPointLogicStep(b.getRegionInfo(c).triarticulation));
            addLogicStep(new RegionChainExtendLogicStep(b.getRegionInfo(c)));
        }
    }
}
