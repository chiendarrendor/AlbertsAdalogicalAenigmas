import grid.logic.flatten.FlattenLogicer;

public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b) {
        b.forEachCell((x,y) -> {
            if (b.inBounds(x+1,y) && b.inBounds(x,y+1)) addLogicStep(new QuadLogicStep(x,y));
            if (b.hasClue(x,y)) {
                addLogicStep(new TerminalLogicStep(x,y));
                if (b.getClue(x,y) > 0) {
                    addLogicStep(new StraightPathOutLogicStep(x,y,b.getClue(x,y)));
                }
            } else {
                addLogicStep(new NonTerminalLogicStep(x,y));
            }
        });
        addLogicStep(new AllCellsConnectedLogicStep());
    }
}
