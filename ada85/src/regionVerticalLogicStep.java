import grid.logic.LogicStatus;
import grid.logic.LogicStep;

// this logic step is created if (x,y) and (x,y+1) both exist and are in the same region
public class regionVerticalLogicStep implements LogicStep<Board> {
    int x;
    int y;
    public regionVerticalLogicStep(int x, int y) { this.x = x; this.y = y; }

    @Override public LogicStatus apply(Board thing) {
        LogicStatus result = LogicStatus.STYMIED;
        CellContents topcc = thing.getCell(x,y);
        CellContents bottomcc = thing.getCell(x,y+1);
        int topsize = topcc.size();
        int botsize = bottomcc.size();
        if (topsize == 0 || botsize == 0) return LogicStatus.CONTRADICTION;

        int toplargest = topcc.getLargest();
        int bottomsmallest = bottomcc.getSmallest();

        topcc.removeEqualSmaller(bottomsmallest);
        bottomcc.removeEqualLarger(toplargest);

        if (topcc.size() == 0 || bottomcc.size() == 0) return LogicStatus.CONTRADICTION;
        if (topcc.size() < topsize) result = LogicStatus.LOGICED;
        if (bottomcc.size() < botsize) result = LogicStatus.LOGICED;

        return result;
    }
}
