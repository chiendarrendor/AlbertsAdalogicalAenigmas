import grid.logic.flatten.FlattenLogicer;

public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b) {
        b.forEachCell((x,y)->addLogicStep(new CellLogicStep(x,y,b.isTerminal(x,y))));
        addLogicStep(new PathsLogicStep());
    }

}
