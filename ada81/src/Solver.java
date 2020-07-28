import grid.logic.flatten.FlattenLogicer;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Solver extends FlattenLogicer<Board> {
    private static List<Point> getRow(Board b,int y) {
        List<Point> result = new ArrayList<>();
        for (int x = 0 ; x < b.getWidth() ; ++x) result.add(new Point(x,y));
        return result;
    }

    private static List<Point> getCol(Board b,int x) {
        List<Point> result = new ArrayList<>();
        for (int y = 0 ; y < b.getHeight() ; ++y) result.add(new Point(x,y));
        return result;
    }



    public Solver(Board b) {
        for (List<Point> pair : b.getRegionPairs()) {
            addLogicStep(new PairLogicStep(pair));
        }
        b.forEachCell((x,y)->{
            addLogicStep(new CellLogicStep(b,x,y));
            if (x == 0 && b.hasRowClue(x,y)) addLogicStep(new LineLogicStep(getRow(b,y),b.getRowClue(x,y),MoveType.POSITIVE));
            if (x != 0 && b.hasRowClue(x,y)) addLogicStep(new LineLogicStep(getRow(b,y),b.getRowClue(x,y),MoveType.NEGATIVE));
            if (y == 0 && b.hasColClue(x,y)) addLogicStep(new LineLogicStep(getCol(b,x),b.getColClue(x,y),MoveType.POSITIVE));
            if (y != 0 && b.hasColClue(x,y)) addLogicStep(new LineLogicStep(getCol(b,x),b.getColClue(x,y),MoveType.NEGATIVE));
        });



    }
}
