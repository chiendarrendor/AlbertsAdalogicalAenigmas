import grid.logic.flatten.FlattenLogicer;

public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b) {
        b.forEachCell((x,y)->addLogicStep(new CellLogicStep(b,x,y)));
        addLogicStep(new PathsLogicStep());
        addLogicStep(new SeparationLogicStep());
        addLogicStep(new MisalignmentLogicStep());
    }
}
