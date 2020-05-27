import grid.logic.flatten.FlattenLogicer;

public class Solver extends FlattenLogicer<Board> {
    public Solver (Board b) {
        addLogicStep(new FillPartialRectangleLogicStep());
        for (char c : b.getCluedRegions()) {
            addLogicStep(new CluedRegionLogicStep(b.getRegionCells(c),b.getRegionClue(c)));
        }
    }
}
