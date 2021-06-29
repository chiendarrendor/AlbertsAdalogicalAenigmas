import grid.logic.LogicStatus;
import grid.logic.LogicStep;

public class ApplyFlattenLogicStep implements LogicStep<Board> {
    Solver s;
    public ApplyFlattenLogicStep(Board b, boolean doFlatten) { s = new Solver(b,false); }

    @Override public LogicStatus apply(Board thing) {
        return s.applyTupleSuccessors(thing);
    }
}
