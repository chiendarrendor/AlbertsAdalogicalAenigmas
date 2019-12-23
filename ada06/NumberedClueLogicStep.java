import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class NumberedClueLogicStep implements LogicStep<AdaBoard> {
    int x;
    int y;
    List<Point> adjacents;
    int target;

    public NumberedClueLogicStep(AdaBoard b,int x, int y, int clueNumber) {
        this.x = x;
        this.y = y;
        target = clueNumber;
        adjacents = b.getVisibleCells(x,y,true);
    }

    @Override public LogicStatus apply(AdaBoard thing) {
        int bulbcount = 0;
        int nobulbcount = 0;
        List<Point> unknowns = new ArrayList<>();

        for (Point p : adjacents) {
            switch(thing.getCell(p.x,p.y)) {
                case BULB: ++bulbcount; break;
                case NOBULB: ++nobulbcount; break;
                case EMPTY: unknowns.add(p);
            }
        }

        if (bulbcount > target) return LogicStatus.CONTRADICTION;
        if (bulbcount + unknowns.size() < target) return LogicStatus.CONTRADICTION;
        if (unknowns.size() == 0) return LogicStatus.STYMIED;

        if (bulbcount == target) {
            unknowns.stream().forEach(p->thing.setCell(p.x,p.y,CellType.NOBULB,
                    "clue at " + x + "," + y + " already has " + target + " bulbs"));
            return LogicStatus.LOGICED;
        }

        if (bulbcount + unknowns.size() == target) {
            unknowns.stream().forEach(p->thing.setCell(p.x,p.y,CellType.BULB,
                    "clue at " + x + "," + y + " only has the right number of empty spaces for bulbs"));
            return LogicStatus.LOGICED;
        }

        return LogicStatus.STYMIED;
    }
}
