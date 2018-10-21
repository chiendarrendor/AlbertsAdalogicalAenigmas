import grid.logic.LogicStatus;
import grid.logic.LogicStep;

public class DotCellLogicStep extends CellLogicStep implements LogicStep<Board>  {
    public DotCellLogicStep(int x, int y) { super(x,y);}

    @Override public LogicStatus apply(Board thing) {
        scan(thing);
        int count = thing.getNumber(getX(),getY());

        if (getPathCount() > count) return LogicStatus.CONTRADICTION;
        if (getPathCount() + getUnknowns().size() < count) return LogicStatus.CONTRADICTION;
        if (getUnknowns().size() == 0) return LogicStatus.STYMIED;

        if (getPathCount() == count) {
            getUnknowns().stream().forEach(d->thing.setEdge(getX(),getY(),d,EdgeState.WALL));
            return LogicStatus.LOGICED;
        }

        if (getPathCount() + getUnknowns().size() == count) {
            getUnknowns().stream().forEach(d->thing.setEdge(getX(),getY(),d,EdgeState.PATH));
            return LogicStatus.LOGICED;
        }

        return LogicStatus.STYMIED;
    }
}
