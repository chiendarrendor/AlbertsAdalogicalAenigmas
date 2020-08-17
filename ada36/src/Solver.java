import grid.logic.flatten.FlattenLogicer;

public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b) {
        b.forEachCell((x,y)-> {
            if (b.hasClue(x,y)) addLogicStep(new ClueLogicStep(x,y,b.getClue(x,y)));
        });
        addLogicStep(new SingleGroupInsideLogicStep());
        addLogicStep(new SingleGroupOutsideLogicStep());
    }
}
