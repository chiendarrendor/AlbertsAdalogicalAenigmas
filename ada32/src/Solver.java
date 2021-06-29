import grid.logic.flatten.FlattenLogicer;

public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b,boolean doFlatten) {
        b.forEachCell((x,y)-> {
            if (b.hasClue(x,y)) {
                if (b.getClueNumber(x,y) == 0) {
                    addLogicStep(new ZeroArrowLogicStep(x,y,b.getClueDirection(x,y)));
                } else {
                    addLogicStep(new ArrowLogicStep(x, y, b.getClueDirection(x, y), b.getClueNumber(x, y)));
                }
            } else {
                addLogicStep(new SnakeAdjacencyLogicStep(x,y));
                addLogicStep(new SnakeHeadLogicStep(x,y));
            }
        });

        addLogicStep(new SnakesCannotTouchLogicStep());

        if (doFlatten) addLogicStep(new ApplyFlattenLogicStep(b,false));

    }
}
