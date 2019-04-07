import grid.logic.flatten.FlattenLogicer;
import grid.solverrecipes.singleloopflatten.CellPathLogicStep;
import grid.solverrecipes.singleloopflatten.SinglePathLogicStep;

public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b) {
        CellPathLogicStep.generateLogicSteps(this,b,false);
        addLogicStep(new SinglePathLogicStep<Board>());

        for (Region r : b.regions.values()) {
            addLogicStep(new RegionEdgeLogicStep(b,r));
        }

        b.forEachCell((x,y)->{
            if (b.isSun(x,y)) addLogicStep(new CosmicCellLogicStep(x,y,RegionStatus.DAY,b.getRegionId(x,y)));
            if (b.isMoon(x,y)) addLogicStep(new CosmicCellLogicStep(x,y,RegionStatus.NIGHT,b.getRegionId(x,y)));
        });

    }
}
