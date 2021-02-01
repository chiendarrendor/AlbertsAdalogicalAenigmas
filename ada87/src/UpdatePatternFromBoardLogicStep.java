import grid.logic.LogicStatus;
import grid.logic.LogicStep;

public class UpdatePatternFromBoardLogicStep implements LogicStep<Board> {
    RegionId rid;
    public UpdatePatternFromBoardLogicStep(RegionId rid) { this.rid = rid; }

    @Override public LogicStatus apply(Board thing) {
        Pattern p = thing.getPattern(rid.getTag());
        return p.updateFromBoard(thing,rid);
    }

    @Override public String toString() {
        return "UpdatePatternFromBoardLogicStep " + rid;
    }
}
