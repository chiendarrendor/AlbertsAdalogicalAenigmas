import grid.logic.flatten.FlattenLogicer;

public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b) {
        b.forEachCell((x,y) -> {
            if (!b.isNumber(x,y)) return;
            addLogicStep(new NumberLogicStep(b,x,y,b.getNumber(x,y)));
        });

    }
}
