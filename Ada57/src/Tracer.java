import grid.puzzlebits.Direction;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public final class Tracer {
    private Tracer() {}

    // this method returns a list of all the points, heading outward from x,y in the direction d
    // until one of the following is encountered:
    //    a) edge of the board
    //    b) a cell with opposing path to direction
    //    c) a clue space
    //    d) if 'includeUnknowns' is false, an UNKNOWN cell

    // in case (c), if includeClue is true, the last item in the list returned will be the cell of that clue
    public static List<Point> trace(Board thing,int x, int y, Direction d,boolean includeUnknowns,boolean includeClue) {
        List<Point> result = new ArrayList<>();
        Point basep = new Point(x,y);

        int index = 1;
        mainloop: while(true) {
            Point curp = d.delta(basep,index);
            if (!thing.inBounds(curp.x,curp.y)) break; // case (a)
            switch(thing.getCell(curp.x,curp.y)) {
                case NUMBER:
                    if (includeClue) result.add(curp); // case (c) note
                    break mainloop; // case(c)
                case HORIZONTAL:
                    if (d == Direction.NORTH || d == Direction.SOUTH) break mainloop; // case (b)
                    result.add(curp);
                    break;
                case VERTICAL:
                    if (d == Direction.EAST || d == Direction.WEST) break mainloop; // case (b)
                    result.add(curp);
                    break;
                case UNKNOWN:
                    if (!includeUnknowns) break mainloop; // case (d)
                    result.add(curp);
                    break;
            }
            ++index;
        }
        return result;
    }
}
