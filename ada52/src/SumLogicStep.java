import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import sun.rmi.runtime.Log;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SumLogicStep implements LogicStep<Board>
{
    int rid;
    int nx;
    int ny;

    int sum;
    List<Point> points;
    public SumLogicStep(int rid,int nx,int ny,int sum, List<Point> points)
    {
        this.rid = rid; this.nx = nx; this.ny = ny;
        this.sum = sum; this.points = points;
    }


    public LogicStatus apply(Board thing)
    {
        List<CellNumbers> cellnumbers = new ArrayList<>();
        LogicStatus result = LogicStatus.STYMIED;

        for (Point p : points) cellnumbers.add(thing.getWorkBlock(p.x,p.y));

        // for each non-done cell a (a not valid cell is a contradiction
        // for each other cell, find its largest value sum(b)
        // for each value of a such that v(a) + sum(b) < sum that value of a is invalid
        //
        // for each non-done cell a (not valid cell is a contradiction)
        // for each other cell, find its smallest value sum(b)
        // for each value of a such that v(a) + sum(b) > sum that value of a is invalid

        for (int i = 0 ; i < cellnumbers.size() ; ++i)
        {
            CellNumbers opcn = cellnumbers.get(i);
            if (!opcn.isValid()) return LogicStatus.CONTRADICTION;
            if (opcn.isDone()) continue;
            int othermaxsum = 0;
            int otherminsum = 0;
            for (int j = 0 ; j < cellnumbers.size() ; ++j)
            {
                if (i == j) continue;
                CellNumbers tcn = cellnumbers.get(j);
                if (!tcn.isValid()) return LogicStatus.CONTRADICTION;
                othermaxsum += tcn.maxVal();
                otherminsum += tcn.minVal();
            }

            for (int v = 1 ; v <= 5 ; ++v)
            {
                if (!opcn.isOn(v)) continue;
                if (v + othermaxsum < sum || v+otherminsum > sum)
                {
                    result = LogicStatus.LOGICED;
                    opcn.removeNumber(v);
                }
            }
        }

        // sum = 0
        // for each cell
        // if any cell is invalid, contradiction
        // if any cell is not done return result
        // if any cell is done, sum += value

        // if (sum != sum) return contradiction

        int finalsum = 0;
        for (CellNumbers cn : cellnumbers)
        {
            if (!cn.isValid()) return LogicStatus.CONTRADICTION;
            if (!cn.isDone()) return result;
            finalsum += cn.doneNumber();
        }

        if (finalsum != sum) return LogicStatus.CONTRADICTION;

        return result;
    }

    public String toString()
    {
        return "SumLogicStep, Rectangle " + rid + "(" + nx + "," + ny + ")";
    }
}
