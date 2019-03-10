package grid.solverrecipes.genericloopyflatten;

import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.util.HashSet;
import java.util.Set;

public class RegionBoundaryLogicStep implements LogicStep<LoopyBoard> {
    int size;
    Set<String> enames;
    @Override public String toString() { return "Region Boundary Logic Step size " + size + ": " + enames; }
    public RegionBoundaryLogicStep(int size, Set<String> clue) { this.size = size; enames = clue; }


    @Override public LogicStatus apply(LoopyBoard thing) {
        int pathcount = 0;
        int nopathcount = 0;
        Set<String> unknowns = new HashSet<>();

        for (String ename : enames) {
            switch(thing.getEdge(ename)) {
                case PATH: ++pathcount; break;
                case NOTPATH: ++nopathcount; break;
                case UNKNOWN: unknowns.add(ename);
            }
        }

        if (pathcount > size) return LogicStatus.CONTRADICTION;
        if (pathcount + unknowns.size() < size) return LogicStatus.CONTRADICTION;
        if (unknowns.size() == 0) return LogicStatus.STYMIED;

        if (pathcount == size) {
            unknowns.stream().forEach(e->thing.setEdge(e,LineState.NOTPATH));
            return LogicStatus.LOGICED;
        }

        if (pathcount + unknowns.size() == size) {
            unknowns.stream().forEach(e->thing.setEdge(e,LineState.PATH));
            return LogicStatus.LOGICED;
        }

        return LogicStatus.STYMIED;
    }
}
