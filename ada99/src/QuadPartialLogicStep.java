import grid.logic.LogicStatus;
import java.awt.Point;

public class QuadPartialLogicStep extends CountingLogicStep {

    public QuadPartialLogicStep(int x, int y) {
        addPoint(new Point(x,y));
        addPoint(new Point(x+1,y));
        addPoint(new Point(x,y+1));
        addPoint(new Point(x+1,y+1));
    }

    @Override public LogicStatus apply(Board thing) {
        count(thing);
        if (shadedcount == 4 || unshadedcount == 4) return LogicStatus.CONTRADICTION;
        if (unknowns.size() == 0) return LogicStatus.STYMIED;
        if (shadedcount == 3) {
            unknowns.stream().forEach(p->thing.setCell(p.x,p.y,CellState.UNSHADED));
            return LogicStatus.LOGICED;
        }
        if (unshadedcount == 3) {
            unknowns.stream().forEach(p->thing.setCell(p.x,p.y,CellState.SHADED));
            return LogicStatus.LOGICED;
        }

        return LogicStatus.STYMIED;
    }
}
