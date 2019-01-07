import grid.logic.flatten.FlattenLogicer;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Solver extends FlattenLogicer<Board> {

    public static List<Point> makeLine(int x1, int y1, int x2, int y2) {
        List<Point> result = new ArrayList<>();
        int dx = (x1 == x2) ? 0 : 1;
        int dy = (y1 == y2) ? 0 : 1;
        if (dx > 0 && dy > 0) throw new RuntimeException("oops...makeLine only designed for orthogonal lines");
        if (dx == 0 && dy == 0) throw new RuntimeException("oops...makeLine does actually need to go somewhere");

        while(true) {
            if (x1 == x2 && y1 == y2) break;
            Point p = new Point(x1,y1);
            result.add(p);
            x1 += dx;
            y1 += dy;
        }
        return result;
    }

    public Solver(Board b) {
        for (int y = 0 ; y < b.getHeight(); ++y) {
            for (int x = 0 ; x < b.getWidth() ; ++x) {
                if (b.getBoardCount() > 1) {
                    addLogicStep(new CrossGridCellLogicStep(x,y));
                }
                for (int bid = 0 ; bid < b.getBoardCount(); ++bid) {
                    addLogicStep(new AdjacentCellLogicStep(b,bid,x,y));
                }
            }
        }

        for (int bid = 0 ; bid < b.getBoardCount(); ++bid) {
            for (int x = 0 ; x < b.getWidth() ; ++x) {
                addLogicStep(new LineLogicStep(bid,b.getSubBoard(bid).getVClue(x),makeLine(x,0,x,b.getHeight())));
            }

            for (int y = 0 ; y < b.getHeight() ; ++y ) {
                addLogicStep(new LineLogicStep(bid,b.getSubBoard(bid).getHClue(y),makeLine(0,y,b.getWidth(),y)));

            }
        }


    }




}
