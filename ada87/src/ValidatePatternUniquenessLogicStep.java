import grid.logic.LogicStatus;
import grid.puzzlebits.CanonicalPointSet;

import java.util.ArrayList;
import java.util.List;

public class ValidatePatternUniquenessLogicStep implements grid.logic.LogicStep<Board> {

    @Override public LogicStatus apply(Board thing) {
        List<Pattern> completes = new ArrayList<>();
        for(char pid : thing.getPatternIds()) {
            Pattern p = thing.getPattern(pid);
            if (!p.isComplete()) continue;
            completes.add(p);
        }

        for (int i = 0 ; i < completes.size() ; ++i) {
            CanonicalPointSet p1 = completes.get(i).getCanonicalPointSet();
            for (int j = i+1 ; j < completes.size() ; ++j) {
                CanonicalPointSet p2 = completes.get(j).getCanonicalPointSet();
                if (p1.equivalent(p2)) return LogicStatus.CONTRADICTION;
            }
        }

        return LogicStatus.STYMIED;
    }
}
