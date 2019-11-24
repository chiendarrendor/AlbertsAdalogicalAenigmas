import grid.logic.flatten.FlattenLogicer;

public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b) {
        for (int y = 0 ; y < b.getHeight() ; ++y) {
            for (int x = 0 ; x < b.getWidth() ; ++x) {
                if (b.hasNumber(x,y)) addLogicStep(new NumberClueLogicStep(x,y,b.getNumber(x,y)));
                addLogicStep(new CellLogicStep(x,y));
                if (x+1 < b.getWidth() && y+1 < b.getHeight()) addLogicStep(new QuadCellLogicStep(x,y));
            }
        }
    }
}
