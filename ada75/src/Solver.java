import grid.logic.flatten.FlattenLogicer;
import grid.puzzlebits.Direction;
import grid.solverrecipes.singleloopflatten.CellPathLogicStep;

public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b) {
        b.forEachCell((x,y)->{
            if (b.hasClue(x,y)) {
                if (b.getClue(x,y) == 1) {
                    addLogicStep(new SingleClueLogicStep(x,y));
                } else {
                    addLogicStep(new EndPathLogicStep(x,y));
                    addLogicStep(new ClueExtendLogicStep(x,y,b.getClue(x,y)));
                }
            } else {
                addLogicStep(new CellPathLogicStep<Board>(x,y,false));
            }
        });
        addLogicStep(new PathQualityLogicStep());
    }


}
