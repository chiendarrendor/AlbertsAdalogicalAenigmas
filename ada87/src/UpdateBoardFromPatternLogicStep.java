import grid.logic.LogicStatus;
import grid.logic.LogicStep;

public class UpdateBoardFromPatternLogicStep implements LogicStep<Board> {
    private RegionId rid;
    public UpdateBoardFromPatternLogicStep(RegionId rid) { this.rid = rid; }

    @Override public LogicStatus apply(Board thing) {
        Pattern p = thing.getPattern(rid.getTag());
        return p.updateToBoard(thing,rid);
    }

    @Override public String toString() {
        return "UpdateBoardFromPatternLogicStep " + rid;
    }
}
