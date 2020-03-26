import grid.logic.flatten.FlattenLogicer;

public class Solver extends FlattenLogicer<CrateBoard> {
    public Solver(CrateBoard b) {
        b.forEachCell((x,y)-> {
            addLogicStep(new TerminalStateLogicStep(x,y));
            addLogicStep(new EmptyCellLogicStep(x,y));
            if (b.hasStartingBox(x,y)) addLogicStep(new CrateLogicStep(x,y));
            if (b.hasClue(x,y)) addLogicStep(new ClueLogicStep(x,y,b.getClueSize(x,y),b.getClueDirection(x,y)));
        });
    }
}
