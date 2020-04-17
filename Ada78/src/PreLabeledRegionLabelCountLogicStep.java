import grid.logic.LogicStatus;
import grid.logic.LogicStep;

public class PreLabeledRegionLabelCountLogicStep implements LogicStep<Board> {
    char rid;
    public PreLabeledRegionLabelCountLogicStep(char rid) { this.rid = rid; }

    @Override public LogicStatus apply(Board thing) {
        Board.CountHolder ch = thing.getRegionCounts(rid);
        int count = thing.getRegionLabel(rid);

        if (ch.labelcount > count) return LogicStatus.CONTRADICTION;
        if (ch.labelcount + ch.unknowns.size() < count) return LogicStatus.CONTRADICTION;
        if (ch.unknowns.size() == 0) return LogicStatus.STYMIED;

        if (ch.labelcount == count) {
            ch.unknowns.stream().forEach(p->thing.setCell(p.x,p.y,CellState.BARRIER));
            return LogicStatus.LOGICED;
        }

        if (ch.labelcount + ch.unknowns.size() == count) {
            ch.unknowns.stream().forEach(p->thing.setCell(p.x,p.y,CellState.LABEL));
            return LogicStatus.LOGICED;
        }

        return LogicStatus.STYMIED;
    }
}
