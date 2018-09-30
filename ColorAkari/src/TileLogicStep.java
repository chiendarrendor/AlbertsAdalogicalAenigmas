import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import sun.rmi.runtime.Log;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class TileLogicStep implements LogicStep<Board> {
    Point myself;
    List<Point> illuminating; // all other cells that can illuminate me
    List<Point> illuminators; // all cells that can illuminate me (including me)

    public TileLogicStep(int x, int y, List<Point> illluminating)
    {
        myself = new Point(x,y);
        this.illuminating = illluminating;
        this.illuminators = new ArrayList<>(illluminating);
        this.illuminators.add(myself);
    }

    @Override public LogicStatus apply(Board thing) {
        LogicStatus result = LogicStatus.STYMIED;
        // item #1.  This cell must be illuminated (including by myseelf)
        List<LightCell> lighters = new ArrayList<>();
        for (Point p : illuminators) {
            LightCell lc = thing.getLightCell(p.x,p.y);
            if (lc.isDark()) continue;
            lighters.add(lc);
        }
        if (lighters.size() == 0) return LogicStatus.CONTRADICTION;
        if (lighters.size() == 1) {
            LightCell lastlc = lighters.get(0);
            if (lastlc.contains(LightState.NOLIGHT)) {
                lastlc.remove(LightState.NOLIGHT);
                result = LogicStatus.LOGICED;
                if (lastlc.isBroken()) return LogicStatus.CONTRADICTION;
            }
        }

        // item #2.   if I am lit, none of my illuminators can be.
        LightCell mylightcell = thing.getLightCell(myself.x,myself.y);
        if (!mylightcell.contains(LightState.NOLIGHT)) {
            for (Point p : illuminating) {
                LightCell mustgodark = thing.getLightCell(p.x,p.y);
                if (!mustgodark.contains(LightState.NOLIGHT)) return LogicStatus.CONTRADICTION;
                if (mustgodark.isDark()) continue;
                result = LogicStatus.LOGICED;
                mustgodark.setAs(LightState.NOLIGHT);
            }
        }

        return result;
    }
}
