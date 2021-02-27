import grid.logic.flatten.FlattenLogicer;

public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b) {
        b.forEachCell((x,y)-> {
            addLogicStep(new CellPointsLogicStep(x,y));
        });
        for(char c : b.getRegionIds()) {
            addLogicStep(new SingleArrowRegionLogicStep(b.getRegionCells(c)));
        }


    }
}
