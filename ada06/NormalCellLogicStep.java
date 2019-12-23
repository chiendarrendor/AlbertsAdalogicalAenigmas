import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class NormalCellLogicStep implements LogicStep<AdaBoard> {
    List<Point> visibles;
    int x;
    int y;

    public NormalCellLogicStep(AdaBoard b,int x, int y) { this.x = x; this.y = y; visibles = b.getVisibleCells(x,y,false); }

    @Override public LogicStatus apply(AdaBoard thing) {
        int numbulbs = 0;
        int numnotbulbs = 0;
        List<Point> unknowns = new ArrayList<>();

        for (Point p : visibles) {
            switch(thing.getCell(p.x,p.y)) {
                case BULB: ++numbulbs; break;
                case NOBULB: ++numnotbulbs; break;
                case EMPTY: unknowns.add(p);
            }
        }

        switch(thing.getCell(x,y)) {
            case BULB:
                if (numbulbs > 0) return LogicStatus.CONTRADICTION;
                if (unknowns.size() == 0) return LogicStatus.STYMIED;
                unknowns.stream().forEach(p->thing.setCell(p.x,p.y,CellType.NOBULB,
                        "cell " + x + "," + y + " is a bulb"));
                return LogicStatus.LOGICED;
            case NOBULB:
                if (numbulbs + unknowns.size() < 1) return LogicStatus.CONTRADICTION;
                if (numbulbs > 0) return LogicStatus.STYMIED;
                if (unknowns.size() == 0) return LogicStatus.STYMIED;
                if (unknowns.size() > 1) return LogicStatus.STYMIED;
                unknowns.stream().forEach(p->thing.setCell(p.x,p.y,CellType.BULB, "" +
                        "cell " + x + "," + y + " is not a bulb and can't see any bulbs or other empty spaces"));
                return LogicStatus.LOGICED;
            case EMPTY:
                if (numbulbs > 0) {
                    thing.setCell(x,y,CellType.NOBULB,"unknown cell " + x + "," + y + " can see other bulbs, so can't be one");
                    return LogicStatus.LOGICED;
                }
                if (numbulbs == 0 && unknowns.size() == 0) {
                    thing.setCell(x,y,CellType.BULB, "unknown cell " + x + "," + y + " can't see any other bulbs or unknowns");
                    return LogicStatus.LOGICED;
                }
                return LogicStatus.STYMIED;
            default:
                throw new RuntimeException("shouldn't have this case!");
        }
    }
}
