import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class InequalityLogicStep implements LogicStep<Board> {
    Point larger;
    Point smaller;

    public String toString() { return "Inequality Logic Step " + larger + " " + smaller; }

    public InequalityLogicStep(int x, int y, Direction inequalityDirection, char inequalitySymbol) {

        Point p1 = new Point(x,y);
        Point p2 = inequalityDirection.delta(p1,1);
        switch(inequalityDirection) {
            case NORTH:
                if (inequalitySymbol == '^') { larger = p1; smaller = p2; }
                else if (inequalitySymbol == 'v') { larger = p2; smaller = p1; }
                else throw new RuntimeException("Unknown NORTH symbol " + inequalitySymbol);
                break;
            case SOUTH:
                if (inequalitySymbol == '^') { larger = p2; smaller = p1; }
                else if (inequalitySymbol == 'v') { larger = p1; smaller = p2; }
                else throw new RuntimeException("Unknown SOUTH symbol " + inequalitySymbol);
                break;
            case EAST:
                if (inequalitySymbol == '<') { larger = p2; smaller = p1; }
                else if (inequalitySymbol == '>') { larger = p1; smaller = p2; }
                else throw new RuntimeException("Unknown EAST symbol " + inequalitySymbol);
                break;
            case WEST:
                if (inequalitySymbol == '<') { larger = p1; smaller = p2; }
                else if (inequalitySymbol == '>') { larger = p2; smaller = p1; }
                else throw new RuntimeException("Unknown WEST symbol " + inequalitySymbol);
                break;
        }
    }

    @Override public LogicStatus apply(Board thing) {
        CellSet smallcs = thing.getCellSet(smaller.x,smaller.y);
        CellSet largecs = thing.getCellSet(larger.x,larger.y);
        if (smallcs.size() == 0) return LogicStatus.CONTRADICTION;
        if (largecs.size() == 0) return LogicStatus.CONTRADICTION;

        int smallsmall = smallcs.smallest();
        int largelarge = largecs.largest();

        Set<Integer> largeremoves = largecs.stream().filter(x->x<=smallsmall).collect(Collectors.toSet());
        Set<Integer> smallremoves = smallcs.stream().filter(x->x>=largelarge).collect(Collectors.toSet());
        if (smallremoves.size() == 0 && largeremoves.size() == 0) return LogicStatus.STYMIED;
        largeremoves.stream().forEach(x->largecs.isNot(x));
        smallremoves.stream().forEach(x->smallcs.isNot(x));
        return LogicStatus.LOGICED;
    }
}
