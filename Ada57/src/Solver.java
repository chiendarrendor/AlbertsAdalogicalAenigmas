import grid.logic.flatten.FlattenLogicer;

public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b) {
        b.forEachCell((x,y) -> {
            //if (b.getCell(x,y) == CellState.UNKNOWN) addLogicStep(new CellLogicStep(x,y));
            if (b.getCell(x,y) == CellState.NUMBER) addLogicStep(new ClueLogicStep(x,y,b.getNumber(x,y)));
        });
    }
}
