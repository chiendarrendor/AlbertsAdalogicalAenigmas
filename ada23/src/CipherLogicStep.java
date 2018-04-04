import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.*;
import java.util.Set;

public class CipherLogicStep implements LogicStep<Board> {
    Point p;
    public CipherLogicStep(Point p) { this.p = p; }

    @Override
    public LogicStatus apply(Board thing) {
        Hole h = thing.holes.get(p);
        Set<Integer> iset = thing.getCharMap().getEntrySet(h.lengthid);
        LogicStatus result = LogicStatus.STYMIED;

        // example: hole: 1 2 4 6  iset: 2 3 4
        //
        // a number in iset must be a length present in this hole to be valid.
        for (int i : iset.toArray(new Integer[0])) {
            if (!h.shotsByLength.containsKey(i)) {
                iset.remove(i);
                result = LogicStatus.LOGICED;
            }
        }
        // result: hole: 1 2 4 6  iset 2 4


        // for a length to be present in hole, it must be present in iset, too
        int maxlen = h.maxLen();
        for (int i = 1 ; i <= maxlen ; ++i) {
            if (!h.shotsByLength.containsKey(i)) continue;
            if (iset.contains(i)) continue;
            result = LogicStatus.LOGICED;
            h.shotsByLength.remove(i);
        }

        return result;
    }
}
