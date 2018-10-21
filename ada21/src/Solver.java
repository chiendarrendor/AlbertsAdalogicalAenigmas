import grid.logic.flatten.FlattenLogicer;

public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b) {
        b.forEachCell((x,y) -> {
            if (b.isDot(x,y)) {
                addLogicStep(new DotCellLogicStep(x,y));
            } else {
                addLogicStep(new NonDotCellLogicStep(x,y));
            }
        });

        addLogicStep(new PathCorrectnessLogicStep());
        addLogicStep(new DotConnectivityLogicStep());
    }
}
