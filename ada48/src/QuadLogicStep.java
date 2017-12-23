import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by chien on 10/14/2017.
 */
public class QuadLogicStep implements LogicStep<Board>
{
    int x;
    int y;
    public QuadLogicStep(int x, int y) { this.x = x; this.y = y; }

    // the rule is "no four regions may meet at a corner"
    // this provides us with a test for the four cells (x,y) (x+1,y),(x,y+1), and (x+1,y+1)
    // 1) there must be at least one adjacent pair of the four cells with at least one
    //    region in common

    public LogicStatus apply(Board thing)
    {
        boolean hasmatch = false;
        if (hasMatch(thing,x,y,x+1,y)) hasmatch = true;
        if (hasMatch(thing,x,y,x,y+1)) hasmatch = true;
        if (hasMatch(thing,x,y+1,x+1,y+1)) hasmatch = true;
        if (hasMatch(thing,x+1,y,x+1,y+1)) hasmatch = true;

        return hasmatch ? LogicStatus.STYMIED : LogicStatus.CONTRADICTION;
    }

    private boolean hasMatch(Board thing, int x1, int y1, int x2, int y2)
    {
        Cell c1 = thing.getCell(x1,y1);
        Cell c2 = thing.getCell(x2,y2);

        Set<Character> into = new HashSet<>(c1.getPossibles());
        into.retainAll(c2.getPossibles());

        return into.size() > 0;
    }
}
