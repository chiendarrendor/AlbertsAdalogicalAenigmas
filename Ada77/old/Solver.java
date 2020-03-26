import grid.logic.flatten.FlattenLogicer;

public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b) {
        b.forEachCell((x,y)-> {
            if (b.hasStartingBox(x,y)) addLogicStep(new CrateLogicStep(x,y));
            if (b.hasClue(x,y)) addLogicStep(new ClueLogicStep(x,y,b.getClueSize(x,y),b.getClueDirection(x,y)));
        });
    }
}
