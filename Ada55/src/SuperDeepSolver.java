import grid.logic.flatten.FlattenLogicer;

public class SuperDeepSolver extends FlattenLogicer<Board> {
    public SuperDeepSolver() {
        addLogicStep(new SuperDeepLogicStep());
    }
}
