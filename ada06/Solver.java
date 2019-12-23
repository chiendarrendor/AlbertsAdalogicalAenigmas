import grid.logic.flatten.FlattenLogicer;

public class Solver extends FlattenLogicer<AdaBoard> {
    public Solver(AdaBoard b) {
        for (int y = 0 ; y < b.getHeight() ; ++y) {
            for (int x = 0 ; x < b.getWidth() ; ++x) {
                if (b.isClue(x,y)) {
                    if (b.isNumberedClue(x,y)) addLogicStep(new NumberedClueLogicStep(b,x,y,b.getClueNumber(x,y)));
                } else {
                    addLogicStep(new NormalCellLogicStep(b,x,y));
                }
            }
        }

    }
}
