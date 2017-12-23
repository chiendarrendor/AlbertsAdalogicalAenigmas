import grid.logic.LogicStatus;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chien on 12/17/2017.
 */
public class CommonOminoElementsLogicStep implements grid.logic.LogicStep<Board>
{
    LogicStatus[] result = new LogicStatus[1];
    public LogicStatus apply(Board thing)
    {
        result[0] = LogicStatus.STYMIED;

        thing.forEachCell( (x,y) -> {
            OminoBoard.OminoCell oc = thing.ob.cells[x][y];
            if (oc.requiredColor != CellColor.BLACK) return;
            if (oc.thePlace != null) return;
            processOneCell(thing,x,y);
        });

        return result[0];
    }

    private class CellIntersector
    {
        int x;
        int y;
        int whitecount = 0;
        int blackcount = 0;
        public CellIntersector(int x,int y) { this.x = x; this.y = y; }
    }


    private void processOneCell(Board thing, int x, int y)
    {
        int opcount = thing.ob.cells[x][y].onplaces.size();
        if (opcount > 10) return;

        Map<Point,CellIntersector> intersections = new HashMap<>();

        for(OminoBoard.OminoPlaceSet.OminoPlace op : thing.ob.cells[x][y].onplaces)
        {
            op.forEachCell((ox,oy,c) -> {
                Point p = new Point(ox,oy);
                if (!intersections.containsKey(p)) intersections.put(p,new CellIntersector(ox,oy));
                if (c == CellColor.BLACK) ++intersections.get(p).blackcount;
                else ++intersections.get(p).whitecount;
            });
        }

        for (CellIntersector ci : intersections.values())
        {
            if (thing.ob.getCellColor(ci.x,ci.y) != CellColor.UNKNOWN) continue;

            if (ci.whitecount == opcount)
            {
                thing.ob.setCellColor(ci.x,ci.y,CellColor.WHITE);
                result[0] = LogicStatus.LOGICED;
            }
            if (ci.blackcount == opcount)
            {
                thing.ob.setCellColor(ci.x,ci.y,CellColor.BLACK);
                result[0] = LogicStatus.LOGICED;
            }

        }
    }
}
