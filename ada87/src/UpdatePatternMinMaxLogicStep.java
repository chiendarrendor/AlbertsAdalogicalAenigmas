import grid.logic.LogicStatus;
import grid.logic.LogicStep;

public class UpdatePatternMinMaxLogicStep implements LogicStep<Board> {
    char pid;
    public UpdatePatternMinMaxLogicStep(char pid) { this.pid = pid; }


    @Override public LogicStatus apply(Board thing) {
        thing.getPattern(pid).tightenBounds();
        return LogicStatus.STYMIED;
    }
}
