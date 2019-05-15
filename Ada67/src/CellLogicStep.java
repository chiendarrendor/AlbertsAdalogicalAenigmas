import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import sun.rmi.runtime.Log;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

public class CellLogicStep implements LogicStep<Board> {
    int x;
    int y;

    public CellLogicStep(int x, int y) { this.x = x; this.y = y; }

    @Override public LogicStatus apply(Board thing) {
        TemplatePointer tp = thing.getCell(x,y);
        if (tp != null) return LogicStatus.STYMIED;

        Set<Integer> possibles = new HashSet<>();
        possibles.addAll(thing.possibles.getCell(x,y).possibles());


        for (int possible : possibles ) {
            if (thing.isTemplatePlaceable(x,y,Region.templates[possible])) continue;
            thing.makeImpossible(x,y,possible);
        }

        possibles = thing.possibles.getCell(x,y).possibles();
        if (possibles.size() == 0) return LogicStatus.CONTRADICTION;
        if (possibles.size() > 1) return LogicStatus.STYMIED;

        int pidx = possibles.iterator().next();

        thing.placeTemplate(x,y,Region.templates[pidx]);

        return LogicStatus.LOGICED;
    }
}
