import grid.logic.LogicStatus;
import grid.logic.LogicStep;

public class EmptyRegionLabelCountLogicStep implements LogicStep<Board> {
    char rid;
    public EmptyRegionLabelCountLogicStep(char rid) { this.rid = rid; }

    @Override public LogicStatus apply(Board thing) {
        Board.CountHolder ch = thing.getRegionCounts(rid);
        if (ch.labelcount + ch.unknowns.size() < 1) return LogicStatus.CONTRADICTION;
        if (ch.unknowns.size() == 0) return LogicStatus.STYMIED;
        if (ch.labelcount + ch.unknowns.size() == 1) {
            ch.unknowns.stream().forEach(p->thing.setCell(p.x,p.y,CellState.LABEL));
            return LogicStatus.LOGICED;
        }
        return LogicStatus.STYMIED;
    }
}
