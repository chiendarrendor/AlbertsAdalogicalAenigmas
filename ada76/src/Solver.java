import grid.logic.flatten.FlattenLogicer;

public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b) {
        b.forEachCell((x,y)->addLogicStep(new AdjacentShapeLogicStep(x,y)));

        for (char rid : b.getRegionIDs()) {
            addLogicStep(new RegionLogicStep(b.getCellsForRegion(rid)));
        }

        addLogicStep(new ConnectivityLogicStep());
    }
}
