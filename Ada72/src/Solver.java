import grid.logic.flatten.FlattenLogicer;
import grid.solverrecipes.singleloopflatten.CellPathLogicStep;

public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b) {
        b.forEachCell((x,y)-> {
            if (b.hasClue(x,y)) {
                addLogicStep(new EndPathLogicStep(x,y));
            } else {
                addLogicStep(new CellPathLogicStep<Board>(x,y,true));
            }
        });

        addLogicStep(new PathQualityLogicStep());

    }
}
