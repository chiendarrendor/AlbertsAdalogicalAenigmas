import grid.logic.flatten.FlattenLogicer;

public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b) {
        addLogicStep(new MinimumLogicStep());
        addLogicStep(new MaximumLogicStep());
    }
}
