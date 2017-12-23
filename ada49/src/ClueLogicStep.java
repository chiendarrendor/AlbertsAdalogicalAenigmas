import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;
import javafx.scene.control.Cell;

import java.awt.*;
import java.util.Vector;

/**
 * Created by chien on 11/8/2017.
 */
public class ClueLogicStep implements LogicStep<Board>
{
    int x;
    int y;

    public ClueLogicStep(int x, int y)
    {
        this.x = x; this.y = y;
    }


    private class AdjacentInfo
    {
        Point p;
        int index;
        Direction d;
        CellState cs;
        boolean onboard;

        public AdjacentInfo(Board b,Point p,int index,Direction d)
        {
            this.p = p ;
            this.index = index;
            this.d = d;
            if (!b.onBoard(p.x,p.y))
            {
                cs = CellState.WHITE;
                onboard = false;
            }
            else
            {
                cs = b.getCell(p.x, p.y);
                onboard = true;
            }
        }
    }


    @Override
    public LogicStatus apply(Board thing)
    {
        LogicStatus result = LogicStatus.STYMIED;

        // step 1.  make sure that the cell itself is white.
        CellState cs = thing.getCell(x,y);
        if (cs == CellState.BLACK) return LogicStatus.CONTRADICTION;
        if (cs == CellState.UNKNOWN)
        {
            thing.setCell(x,y,CellState.WHITE);
            result = LogicStatus.LOGICED;
        }

        ClueUnrolled cu = thing.unrolls[x][y];


        int index = 0;
        AdjacentInfo ais[] = new AdjacentInfo[8];
        for (Direction d : Direction.values())
        {
            ais[index] = new AdjacentInfo(
                    thing,
                    new Point(x + d.DX(), y + d.DY()),
                    index,
                    d);
            ++index;
        }

        StringBuffer sb = new StringBuffer();
        for (int i = 0 ; i < 8 ; ++i)
        {
            switch(ais[i].cs)
            {
                case UNKNOWN: sb.append('.'); break;
                case BLACK: sb.append('B'); break;
                case WHITE: sb.append('W'); break;
            }
        }

        cu.Restrict(sb.toString());
        if (!cu.isValid()) return LogicStatus.CONTRADICTION;

        String isect = cu.intersection();

        for (int i = 0 ; i < 8 ; ++i)
        {
            char c = isect.charAt(i);

            if (!ais[i].onboard)
            {
                if (c != 'W') throw new RuntimeException("ClueUnrolled didn't make off board white!");
                continue;
            }

            if (c == '.') continue;
            if (c == 'W' && ais[i].cs == CellState.WHITE) continue;
            if (c == 'W' && ais[i].cs == CellState.BLACK) return LogicStatus.CONTRADICTION;
            if (c == 'B' && ais[i].cs == CellState.BLACK) continue;
            if (c == 'B' && ais[i].cs == CellState.WHITE) return LogicStatus.CONTRADICTION;

            result = LogicStatus.LOGICED;
            if (c == 'W') thing.setCell(ais[i].p.x, ais[i].p.y, CellState.WHITE);
            if (c == 'B') thing.setCell(ais[i].p.x, ais[i].p.y, CellState.BLACK);
        }

        return result;
    }
}
