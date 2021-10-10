import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


// given a Board position and a List of cells, will go through every
// permutation of valid numbers in those cells and call upon the child class to validate whether each permutation is valid.
// goal is to find, for each cell, if there are any numbers that don't belong.
abstract public class PermutationExhaustionLogicStep implements LogicStep<Board> {
    private List<Point> cells;
    abstract boolean isPermutationValid(int[] permutation);
    int appliedCount;
    int maxDepth;
    int grid[];

    List<Set<Integer>> inactives = new ArrayList<>();
    List<Cell> realCells = new ArrayList<>();

    private void inactiveSetup() { for (Point p : cells) inactives.add(new HashSet<>()); }

    private void inactivePrepare(Board thing) {
        realCells.clear();
        for (int i = 0 ; i < cells.size(); ++i) {
            Point p = cells.get(i);
            inactives.get(i).clear();
            inactives.get(i).addAll(thing.getCell(p.x,p.y).contents());
            realCells.add(thing.getCell(p.x,p.y));
        }
        appliedCount = 0;
    }

    private void applyInactive(int[] grid) {
        ++appliedCount;
        for (int i = 0 ; i < cells.size() ; ++i) inactives.get(i).remove(grid[i]);
    }

    public void init(List<Point> cells) {
        this.cells = cells;
        maxDepth = cells.size() - 1;
        grid = new int[cells.size()];
        inactiveSetup();
    }

    private void recurse(int curindex) {
        for(int v : realCells.get(curindex).contents()) {
            grid[curindex] = v;

            if (curindex == maxDepth) {
                if (isPermutationValid(grid)) {
                    applyInactive(grid);
                }
            } else {
                recurse(curindex+1);
            }
        }
    }

    @Override public LogicStatus apply(Board thing) {
        for (Point p : cells) {
            if (!thing.getCell(p.x,p.y).isValid()) return LogicStatus.CONTRADICTION;
        }

        inactivePrepare(thing);
        recurse(0);

        if (appliedCount == 0) return LogicStatus.CONTRADICTION;

        // if we get here, then every cell was applied at least once, which means we won't invalidate any cells.
        LogicStatus result = LogicStatus.STYMIED;

        for (int i = 0 ; i < cells.size() ; ++i) {
            Point p = cells.get(i);
            Cell c = realCells.get(i);
            Set<Integer> neverMentioned = inactives.get(i);

            for (int v : neverMentioned) {
                result = LogicStatus.LOGICED;
                c.clear(v);
            }
        }

        return result;
    }
}
