import grid.logic.flatten.FlattenLogicer;
import grid.logic.simple.Logicer;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Solver extends Logicer<Board> {
    public Solver(Board b) {

        for (int y = 0 ; y < b.getHeight() ; ++y) {
            for (int x = 0; x < b.getWidth() ; ++x) {
                addLogicStep(new NoAdjacentsLogicStep(b,x,y));
            }
        }

        for (int fixed = 0 ; fixed < b.getWidth(); ++fixed) {
            List<Point> row = new ArrayList<>();
            List<Point> column = new ArrayList<>();

            for (int moving = 0 ; moving < b.getWidth() ; ++moving) {
                row.add(new Point(moving,fixed));
                column.add(new Point(fixed,moving));
            }
            addLogicStep(new LineContainsKnightsLogicStep(row,b.getKnightsPerLine()));
            addLogicStep(new LineContainsKnightsLogicStep(column,b.getKnightsPerLine()));

        }

        for (int knightid : b.getKnightKeys()) {
            addLogicStep(new KnightLogicStep(knightid));
        }




        addLogicStep(new NoExtraMustHaveKnightLogicStep());
    }
}
