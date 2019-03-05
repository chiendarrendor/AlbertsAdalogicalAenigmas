import grid.logic.flatten.FlattenLogicer;

public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b) {
        b.getRegions().stream().forEach(r-> {
            addLogicStep(new RegionLogicStep(r));
            addLogicStep(new NoAdjacentRegionLogicStep(r));
            addLogicStep(new ExclusionZoneLogicStep(r));
            addLogicStep(new EmptyRegionLogicStep());
        });
    }
}
