import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UniqueLogicStep implements LogicStep<Board>
{
    List<Point> points;
    int maxn = 5;

    public UniqueLogicStep(List<Point> points) { this.points = points; if (points.size() != maxn) throw new RuntimeException("Unique must have exactly " + maxn + " cells!"); }

    public LogicStatus apply(Board thing)
    {
        LogicStatus result = LogicStatus.STYMIED;
        List<CellNumbers> cns = new ArrayList<>();
        for (int i = 0 ; i < points.size() ; ++i) cns.add(thing.getWorkBlock(points.get(i).x,points.get(i).y));


        // check 1: any cell that is 'done' denies that number to all other cells
        for (int i = 0 ; i < cns.size() ; ++i)
        {
            if (!cns.get(i).isDone()) continue;
            int dn = cns.get(i).doneNumber();
            for (int j = 0 ; j < cns.size() ; ++j)
            {
                if (i == j) continue;
                if (!cns.get(j).isOn(dn)) continue;
                result = LogicStatus.LOGICED;
                cns.get(j).removeNumber(dn);
            }
        }

        // any number that has only one cell, that one cell has to be container for the number
        Map<Integer,ArrayList<CellNumbers>> numbermap = new HashMap<>();
        for (CellNumbers cn : cns)
        {
            for (int i = 1 ; i <= maxn ; ++i )
            {
                if (!cn.isOn(i)) continue;
                if (!numbermap.containsKey(i)) numbermap.put(i,new ArrayList<CellNumbers>());
                numbermap.get(i).add(cn);
            }
        }

        if (numbermap.size() != maxn) return LogicStatus.CONTRADICTION;
        for (int i = 1 ; i <= maxn ; ++i)
        {
            List<CellNumbers> cl = numbermap.get(i);
            if (cl.size() > 1) continue;
            CellNumbers cn = cl.get(0);
            if (!cn.isValid()) return LogicStatus.CONTRADICTION;
            if (cn.isDone() && cn.doneNumber() != i) return LogicStatus.CONTRADICTION;
            if (cn.isDone()) continue;
            result = LogicStatus.LOGICED;
            cn.setNumber(i);
        }




        return result;
    }
}
