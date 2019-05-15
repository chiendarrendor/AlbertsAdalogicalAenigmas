import grid.logic.flatten.FlattenLogicer;

public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b) {
        b.forEachCell((x,y)-> {
            if (b.isBlock(x,y)) return;
            addLogicStep(new CellLogicStep(x,y));
        });
        addLogicStep(new AdjacentRegionLogicStep());
    }

}
