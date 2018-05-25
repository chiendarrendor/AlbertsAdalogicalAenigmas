import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.logic.flatten.FlattenLogicer;

public class SuperDeepLogicStep implements LogicStep<Board> {
    Solver s = new Solver(null);
    boolean[] result = new boolean[1];

    @Override
    public LogicStatus apply(Board thing) {
        Board old = new Board(thing);


        while(true) {
            FlattenLogicer.RecursionStatus rs1 = s.recursiveApplyLogic(thing);
            if (rs1 == FlattenLogicer.RecursionStatus.DEAD) return LogicStatus.CONTRADICTION;
            if (rs1 == FlattenLogicer.RecursionStatus.DONE) return LogicStatus.LOGICED;

            LogicStatus ats = s.applyTupleSuccessors(thing);
            if (ats == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
            if (ats == LogicStatus.STYMIED) break;
        }

        result[0] = false;
        old.getEdges().forEachEdge((x,y,isV,es) -> {
            if (es != thing.getEdges().getEdge(x,y,isV)) result[0] = true;
        });

        return result[0] ? LogicStatus.LOGICED : LogicStatus.STYMIED;
    }
}
