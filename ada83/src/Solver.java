import grid.logic.flatten.FlattenLogicer;

public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b) {
        b.forEachCell((x,y)-> {
            addLogicStep(new CellLogicStep(x,y));
        });
        for (char rid : b.getRegionIds()) {
            if (!b.regionHasClue(rid)) continue;
            addLogicStep(new RegionLogicStep(b.getRegionCells(rid),b.getRegionClue(rid)));
        }
        addLogicStep(new PathsLogicStep());
    }
}
