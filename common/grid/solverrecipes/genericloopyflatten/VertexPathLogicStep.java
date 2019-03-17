package grid.solverrecipes.genericloopyflatten;

import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.util.HashSet;
import java.util.Set;

public class VertexPathLogicStep implements LogicStep<LoopyBoard> {
    String vname;
    Set<String> enames;
    boolean demanded;

    @Override public String toString() { return "LoopyBoard VertexPathLogicStep " + vname; }

    public VertexPathLogicStep(String vname, Set<String> enames,boolean demanded) {
        this.vname = vname;
        this.enames = enames;
        this.demanded = demanded;
    }

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

        // only allowable cases: # of paths = 2, or # of paths = 0;
        if (pathcount > 2) return LogicStatus.CONTRADICTION;
        if (pathcount == 2) {
            if (unknowns.size() == 0) return LogicStatus.STYMIED;
            unknowns.stream().forEach(e->thing.setEdge(e,LineState.NOTPATH));
            return LogicStatus.LOGICED;
        }
        if (pathcount == 1) {
            if (unknowns.size() == 0) return LogicStatus.CONTRADICTION;
            if (unknowns.size() > 1) return LogicStatus.STYMIED;
            thing.setEdge(unknowns.iterator().next(),LineState.PATH);
            return LogicStatus.LOGICED;
        }
        // if we get here, we have no paths....it's a combination of NOPATH and unknowns.
        if (demanded) {
            if (unknowns.size() < 2) return LogicStatus.CONTRADICTION;
            if (unknowns.size() == 2) {
                unknowns.stream().forEach(e->thing.setEdge(e,LineState.PATH));
                return LogicStatus.LOGICED;
            }
            return LogicStatus.STYMIED;
        }


        if (unknowns.size() == 1) {
            thing.setEdge(unknowns.iterator().next(),LineState.NOTPATH);
            return LogicStatus.LOGICED;
        }

        return LogicStatus.STYMIED;
    }
}
