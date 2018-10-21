import grid.logic.LogicStatus;
import grid.logic.LogicStep;

public class NonDotCellLogicStep extends CellLogicStep implements LogicStep<Board> {
    public NonDotCellLogicStep(int x, int y) { super(x,y); }

    @Override public LogicStatus apply(Board thing) {
        scan(thing);

        switch(getPathCount()) {
            case 4:
            case 3:
                return LogicStatus.CONTRADICTION;
            case 2:
                if (getUnknowns().size() == 0) return LogicStatus.STYMIED;
                getUnknowns().stream().forEach(d->thing.setEdge(getX(),getY(),d,EdgeState.WALL));
                return LogicStatus.LOGICED;
            case 1:
                if (getUnknowns().size() == 0) return LogicStatus.CONTRADICTION;
                if (getUnknowns().size() > 1) return LogicStatus.STYMIED;
                getUnknowns().stream().forEach(d->thing.setEdge(getX(),getY(),d,EdgeState.PATH));
                return LogicStatus.LOGICED;
            case 0:
                break;
        }
        // if we get here, we have no paths.
        switch(getWallCount()) {
            case 4:
                if (thing.articulates(getX(),getY())) return LogicStatus.CONTRADICTION;
                return LogicStatus.STYMIED;
            case 3:
                if (thing.articulates(getX(),getY())) return LogicStatus.CONTRADICTION;
                getUnknowns().stream().forEach(d->thing.setEdge(getX(),getY(),d,EdgeState.WALL));
                return LogicStatus.LOGICED;
            case 2:
                if (thing.articulates(getX(),getY())) {
                    getUnknowns().stream().forEach(d->thing.setEdge(getX(),getY(),d,EdgeState.PATH));
                    return LogicStatus.LOGICED;
                }
                return LogicStatus.STYMIED;
            case 1:
            case 0:
                break;
        }



        return LogicStatus.STYMIED;
    }
}
