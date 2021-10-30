import grid.logic.LogicStatus;


public class NoExtraMustHaveKnightLogicStep implements grid.logic.LogicStep<Board> {
    @Override public LogicStatus apply(Board thing) {
        int moveableKnights = 0;
        int requiredKnightSpaces = 0;

        for (int kkey : thing.getKnightKeys()) {
            Knight knight = thing.getKnight(kkey);
            if (knight.numPaths() == 0) return LogicStatus.CONTRADICTION;
            if (knight.numPaths() > 1) ++moveableKnights;

        }

        for (int y = 0 ; y < thing.getHeight() ; ++y) {
            for (int x = 0 ; x < thing.getWidth() ; ++x) {
                if (thing.getCell(x,y) == CellState.MUST_HAVE_KNIGHT) ++requiredKnightSpaces;
            }
        }

        if (requiredKnightSpaces > moveableKnights) return LogicStatus.CONTRADICTION;
        return LogicStatus.STYMIED;
    }
}
