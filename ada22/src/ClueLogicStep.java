import grid.logic.simple.LogicStatus;
import grid.logic.LogicStep;

import java.awt.*;

/**
 * Created by chien on 5/19/2017.
 */
public class ClueLogicStep implements LogicStep<Board>
{
    @Override
    public LogicStatus apply(Board thing)
    {
        LogicStatus result = LogicStatus.STYMIED;

        for (Clue c : thing.getClues())
        {
            // blacked clues don't have to be validated
            if (thing.getCell(c.x,c.y) == CellState.BLACK) continue;

            boolean iscon = isContradiction(thing,c);
            if (iscon)
            {
                if (thing.getCell(c.x,c.y) == CellState.WHITE) return LogicStatus.CONTRADICTION;
                // if we get here, clue's cell is unknown but a contradiction; means clue cell must be black.
                thing.setCellBlack(c.x,c.y);
                result = LogicStatus.LOGICED;
                continue;
            }

            // if we get here, no contradiction...but cells only must change if
            // we know clue cell is white.
            if (thing.getCell(c.x, c.y) != CellState.WHITE) continue;

            LogicStatus stat = applyToOne(thing, c);
            if (stat == LogicStatus.LOGICED) result = LogicStatus.LOGICED;
        }
        return result;
    }

    int numblacks = 0;
    EmptyAdjacencyBlocks eab = null;


    private boolean isContradiction(Board thing, Clue c)
    {
        numblacks = 0;
        eab = new EmptyAdjacencyBlocks();

        int i = 1;
        while(true)
        {
            Point p = new Point(c.x + i * c.dir.dx(), c.y + i * c.dir.dy());
            if (!thing.onBoard(p)) break;
            switch (thing.getCell(p.x, p.y))
            {
                case UNKNOWN:
                    eab.addCell(p);
                    break;
                case BLACK:
                    ++numblacks;
                    break;
                case WHITE:
                    break;
            }

            ++i;
        }
        if (numblacks > c.dist) return true;
        if (numblacks + eab.maxBlackCount() < c.dist) return true;
        return false;
    }



    private LogicStatus applyToOne(Board thing, Clue c)
    {
        LogicStatus result = LogicStatus.STYMIED;

        if (eab.maxBlackCount() == 0) return LogicStatus.STYMIED;
        if (numblacks == c.dist)
        {
            int count = eab.whiteFill(thing);
            if (count > 0) result = LogicStatus.LOGICED;
        }
        else if (numblacks + eab.maxBlackCount() == c.dist)
        {
            int count = eab.blackFill(thing);
            if (count > 0) result = LogicStatus.LOGICED;
        }

        return result;
    }


}
