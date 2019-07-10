import grid.logic.flatten.FlattenLogicer;
import grid.solverrecipes.singleloopflatten.CellPathLogicStep;
import grid.solverrecipes.singleloopflatten.SinglePathLogicStep;


public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b) {
        CellPathLogicStep.generateLogicSteps(this,b,false);
        b.forEachCell((x,y)-> {
            if (b.isBlock(x,y)) addLogicStep(new BlockLogicStep(x,y));
        });

        addLogicStep(new StartSpaceLogicStep(b.getStartCell()));

        for (Gate g : b.getGates()) {
            addLogicStep(new GateLogicStep(g));
        }


        addLogicStep(new SinglePathLogicStep<Board>());
        addLogicStep(new NumberedGatePathLogicStep());
    }
}
