import java.awt.*;
import java.util.Vector;

/**
 * Created by chien on 4/22/2017.
 */
public class NotAdjacentLogicStep implements LogicStep<Board>
{

    @Override
    public LogicStatus apply(Board thing)
    {
        LogicStatus result = LogicStatus.STYMIED;
        for (int x = 0 ; x < thing.getWidth() ; ++x)
        {
            for (int y = 0 ; y < thing.getHeight() ; ++y)
            {
                if (thing.getCell(x,y) != CellType.TREE) continue;
                Vector<Point> adjacents = thing.adjacents(x,y);
                for (Point p : adjacents)
                {
                    if (thing.getCell(p.x,p.y) == CellType.TREE) return LogicStatus.CONTRADICTION;
                    if (thing.getCell(p.x,p.y) == CellType.UNKNOWN)
                    {
                        result = LogicStatus.LOGICED;
                        thing.setCell(p.x,p.y,CellType.EMPTY);
                    }
                }
            }
        }
        return result;
    }
}
