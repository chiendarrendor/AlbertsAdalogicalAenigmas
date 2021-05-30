import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TowerHidingLogicStep implements LogicStep<Board> {
    List<Point> cells;
    int clue;

    public TowerHidingLogicStep(List<Point> cells, int clue) { this.cells = cells; this.clue = clue; }

    // called if clue = 1.   the only thing we can know in this case is that the first cell
    // must be the largest value
    private LogicStatus unitaryApply(Board b) {
        Point first = cells.get(0);
        int largest = cells.size();
        CellState cs = b.getCell(first.x,first.y);
        if (cs.broken()) return LogicStatus.CONTRADICTION;
        if (!cs.contains(largest)) return LogicStatus.CONTRADICTION;
        if (cs.complete()) return LogicStatus.STYMIED;
        cs.set(largest);
        return LogicStatus.LOGICED;
    }

    // called if clue == size of row/column...we know the precise value of every cell.
    private LogicStatus fullSlopeApply(Board b) {
        LogicStatus result = LogicStatus.STYMIED;
        for (int i = 0 ; i < cells.size() ; ++i) {
            int number = i+1;
            Point curp = cells.get(i);
            CellState cs = b.getCell(curp.x,curp.y);
            if (cs.broken()) return LogicStatus.CONTRADICTION;
            if (!cs.contains(number)) return LogicStatus.CONTRADICTION;
            if (cs.complete()) continue;
            cs.set(number);
            result = LogicStatus.LOGICED;
        }
        return result;
    }

    // for clues larger than 1:
    // the first cell cannot be any of the top clue-1 values
    // the second cell cannot be any of the top clue-2 values
    // repeat until 'clue - k' is zero
    private LogicStatus basicSlopeApply(Board b) {
        LogicStatus result = LogicStatus.STYMIED;
        for (int i = 0 ; i < cells.size() ; ++i) {
            int cellid = i+1;
            int fromtop = clue - cellid;
            Point curp = cells.get(i);
            CellState cs = b.getCell(curp.x,curp.y);
            for (int j = 0 ; j < fromtop ; ++j) {
                int number = cells.size() - j;

                if (cs.broken()) return LogicStatus.CONTRADICTION;
                if (!cs.contains(number)) continue;
                cs.remove(number);
                result = LogicStatus.LOGICED;
                if (cs.broken()) return LogicStatus.CONTRADICTION;
            }
        }
        return result;
    }

    private boolean matchesClue(int[] permutation) {
        int largestseen = 0;
        int upcount = 0;
        for (int i : permutation) {
            if (i > largestseen) {
                largestseen = i;
                ++upcount;
            }
        }
        return upcount == clue;
    }

    private int[] copy(int[] input) {
        int[] result = new int[cells.size()];
        for (int i = 0 ; i < cells.size() ; ++i) result[i] = input[i];
        return result;
    }


    private void recursivePermute(Board b,int opindex,Set<Integer> unused,List<int[]>finals,int[] prefix) {
        int nextindex = opindex+1;
        for (int number = 1 ; number <= cells.size() ; ++ number) {
            Point p = cells.get(opindex);
            CellState cs = b.getCell(p.x,p.y);
            if (!unused.contains(number)) continue;
            if (!cs.contains(number)) continue;
            int[] newpermutation = copy(prefix);

            unused.remove(number);
            newpermutation[opindex] = number;
            if (nextindex == cells.size()) {
                if (matchesClue(newpermutation)) {
                    finals.add(newpermutation);
                }
            } else {
                recursivePermute(b,nextindex,unused,finals,newpermutation);
            }
            unused.add(number);
        }
    }





    // calculate all legal permutations of the numbers (note that impossibilities are possible)
    //  and determine which of them match the given clue (if zero, is contradiction)
    // for each cell, any number that is not present for any permutation can be removed
    // for each cell, any number that is present for all permutations can be set.
    private LogicStatus fullPermuteApply(Board b) {
        List<int[]> permutations = new ArrayList<>();
        Set<Integer> unused = new HashSet<>();
        for (int i = 1 ; i <= cells.size() ; ++i) unused.add(i);
        int[] basic = new int[cells.size()];
        recursivePermute(b,0,unused,permutations,basic);

        if (permutations.size() == 0) return LogicStatus.CONTRADICTION;
        LogicStatus result = LogicStatus.STYMIED;
        for (int i = 0 ; i < cells.size() ; ++i) {
            Point p = cells.get(i);
            CellState cs = b.getCell(p.x,p.y);
            Set<Integer> notPresents = new HashSet<>();
            for (int j = 1 ; j <= cells.size() ; ++j) notPresents.add(j);
            Set<Integer> presents = new HashSet<>();
            for(int[] perm : permutations) {
                notPresents.remove(perm[i]);
                presents.add(perm[i]);
            }
            for(int np : notPresents) {
                if (cs.contains(np)) {
                    cs.remove(np);
                    result = LogicStatus.LOGICED;
                }
            }

            if (presents.size() == 1 && !cs.complete()) {
                cs.set(presents.iterator().next());
                result = LogicStatus.LOGICED;
            }

        }
        return result;
    }




    @Override public LogicStatus apply(Board thing) {

        LogicStatus result = LogicStatus.STYMIED;
        if (clue == 1) return unitaryApply(thing);
        if (clue == cells.size()) return fullSlopeApply(thing);

        LogicStatus bsa = basicSlopeApply(thing);
        if (bsa == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
        if (bsa == LogicStatus.LOGICED) result = LogicStatus.LOGICED;

        LogicStatus fpa = fullPermuteApply(thing);
        if (fpa == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
        if (fpa == LogicStatus.LOGICED) result = LogicStatus.LOGICED;

        return result;
    }
}
