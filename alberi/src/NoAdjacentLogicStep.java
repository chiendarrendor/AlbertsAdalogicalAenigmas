import grid.logic.LogicStep;
import grid.logic.simple.LogicStatus;

/**
 * Created by chien on 5/27/2017.
 */
public class NoAdjacentLogicStep implements LogicStep<Board>
{
    @Override
    public LogicStatus apply(Board thing)
    {
        LogicStatus result = LogicStatus.STYMIED;
        for (int x = 0 ; x < thing.getWidth() ; ++x)
        {
            for (int y = 0 ; y < thing.getHeight() ; ++y)
            {
                if (thing.getCell(x,y) != CellState.TREE) continue;
                LogicStatus lstat = applyToOne(thing,x,y);
                if (lstat == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
                if (lstat == LogicStatus.LOGICED) result = LogicStatus.LOGICED;
            }
        }
        return result;
    }

    private LogicStatus applyToOne(Board thing, int x, int y)
    {
        LogicStatus result = LogicStatus.STYMIED;
        for (int dx = -1 ; dx <= 1 ; ++dx)
        {
            for (int dy = -1 ; dy <= 1 ; ++dy)
            {
                if (dx == 0 && dy == 0) continue;
                int nx = x + dx;
                int ny = y + dy;

                if (nx < 0 || ny < 0 || nx >= thing.getWidth() || ny >= thing.getHeight()) continue;
                if (thing.getCell(nx,ny) == CellState.TREE) return LogicStatus.CONTRADICTION;
                if (thing.getCell(nx,ny) == CellState.GRASS) continue;

                thing.setCellGrass(nx,ny);
                result =LogicStatus.LOGICED;
            }
        }
        return result;
    }
}
