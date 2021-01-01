import grid.lambda.CellLambda;
import grid.logic.flatten.FlattenLogicer;

import java.awt.Point;

public class Solver extends FlattenLogicer<Board> {

    public Solver(Board b) {
        CellLambda.forEachCell(b.getWidth(),b.getHeight(),(x,y)->{
            addLogicStep(new CellLogicStep(x,y));
            if (b.hasTile(x,y)) {
                addLogicStep(new TileLogicStep(x,y));
            }
        });

    }
}
