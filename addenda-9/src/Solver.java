import grid.lambda.CellLambda;
import grid.logic.flatten.FlattenLogicer;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class Solver extends FlattenLogicer<Board>
{
    public Solver(Board b)
    {

        Set<Point> seenPoints = new HashSet<Point>();

        CellLambda.forEachCell(b.getWidth(),b.getHeight(),(x,y)->{
            addLogicStep(new CellPathLogicStep(x,y));
            if (b.getCellColor(x,y) == CellColor.UNCOLORED) return;
            addLogicStep(new DotLogicStep(x,y));

            if (b.getCellColor(x,y) != CellColor.UNKNOWN) return;

            Point curP = new Point(x,y);
            Point otherP = new Point(b.getWidth()-1-x,b.getHeight()-1-y);

            if (seenPoints.contains(curP)) return;
            seenPoints.add(otherP);
            addLogicStep(new CellColorPairLogicStep(curP.x,curP.y,otherP.x,otherP.y));

        });
       addLogicStep(new WholePathLogicStep());
    }

}
