import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TargetLogicStep implements LogicStep<Board> {
    GroundState mycolor;
    List<Point> illuminating;
    private static LightState[] lights = new LightState[]{ LightState.REDLIGHT,LightState.GREENLIGHT,LightState.BLUELIGHT};
    public TargetLogicStep(GroundState gs, List<Point> illluminating) { mycolor = gs; this.illuminating = illluminating; }

    @Override public LogicStatus apply(Board thing) {
        LogicStatus result = LogicStatus.STYMIED;
        Map<LightState,List<LightCell>> lighttypes = new HashMap<>();
        for(LightState light : lights) lighttypes.put(light,new ArrayList<>());

        // organize all potentially illumminating cells by what color they illuminate
        for (Point p : illuminating) {
            LightCell ls = thing.getLightCell(p.x,p.y);
            for (LightState light : lights) {
                if (ls.contains(light)) lighttypes.get(light).add(ls);
            }
        }

        // every target has an anticolor...we cannot be illuminated with that color
        LightState anticolor = mycolor.getAnticolor();
        for (LightCell ls : lighttypes.get(anticolor)) {
            result = LogicStatus.LOGICED;
            ls.remove(anticolor);
            if (ls.isBroken()) return LogicStatus.CONTRADICTION;
        }
        lighttypes.remove(anticolor);

        // the other two colors must be represented at least once each
        for (LightState light : lighttypes.keySet()) {
            List<LightCell> colorcells = lighttypes.get(light);

            if (colorcells.size() == 0) return LogicStatus.CONTRADICTION;
            if (colorcells.size() > 1) continue;
            LightCell lastcell = colorcells.get(0);
            if (!lastcell.contains(light)) return LogicStatus.CONTRADICTION;
            if (lastcell.isComplete()) continue;
            lastcell.setAs(light);
            result = LogicStatus.LOGICED;
        }



        return result;
    }
}
