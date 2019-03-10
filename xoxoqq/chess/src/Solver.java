import grid.logic.flatten.FlattenLogicer;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Solver extends FlattenLogicer<Board> {

    private static List<Point> getLine(int x, int y, Direction d, int count) {
        List<Point> result = new ArrayList<>();
        Point cp = new Point(x,y);
        for (int i = 0 ; i < count ; ++i ) result.add(d.delta(cp,i));
        return result;
    }



    public Solver(Board b) {
        b.forEachCell((x,y)-> {
            if (b.hasInequality(x,y))
                addLogicStep(new InequalityLogicStep(x,y,b.getInequalityDirection(x,y),b.getInequalitySymbol(x,y)));
            if (b.hasDiff(x,y))
               addLogicStep(new DiffLogicStep(x,y,b.getDiffDirection(x,y),b.getDiffSize(x,y)));
        });

        for (int x = 0 ; x < b.getWidth() ; ++x) addLogicStep(new LineLogicStep(getLine(x,0,Direction.SOUTH,b.getHeight())));
        for (int y = 0 ; y < b.getHeight() ; ++y) addLogicStep(new LineLogicStep(getLine(0,y,Direction.EAST,b.getWidth())));

    }
}
