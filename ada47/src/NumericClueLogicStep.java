import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

public class NumericClueLogicStep implements LogicStep<LogicBoard> {
    int x;
    int y;
    int targetlen;

    public NumericClueLogicStep(int x, int y, int numericClue) { this.x = x; this.y = y; this.targetlen = numericClue; }

    @Override public LogicStatus apply(LogicBoard thing) {

        // have to anti-count one of the two ends since they overlap here.
        int min = -1;
        int max = -1;

        for (Direction d : Direction.orthogonals()) {
            if (thing.getEdge(x,y,d) != EdgeType.PATH) continue;
            CluePair cp = thing.getStraightMinMax(x,y,d);
            min += cp.min;
            max += cp.max;
        }

        if (max < targetlen) return LogicStatus.CONTRADICTION;
        if (min > targetlen) return LogicStatus.CONTRADICTION;
        return LogicStatus.STYMIED;
    }
}
