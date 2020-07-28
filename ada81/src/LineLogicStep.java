import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class LineLogicStep implements LogicStep<Board> {
    List<Point> points;
    int count;
    MoveType type;

    public LineLogicStep(List<Point> row, int count, MoveType type) { points = row; this.count = count; this.type = type; }

    private List<List<Cell>> makeBlocks(Board thing) {
        List<List<Cell>> result = new ArrayList<>();
        List<Cell> curlist = new ArrayList<>();
        result.add(curlist);

        for(Point p : points) {
            Cell c = thing.getCell(p.x,p.y);
            if (c.isBroken()) return null;
            if (c.isBlank()) {
                curlist = new ArrayList<>();
                result.add(curlist);
            } else {
                curlist.add(c);
            }
        }
        return result;
    }

    private class BlockStatus {
        int actual = 0;
        int possible = 0;
        int maxsize = 0;
        List<Cell> cells;

        // note this takes a list of cells instead of using points because we're operating on a block, not the whole line
        public BlockStatus(Board thing,List<Cell> cells) {
            this.cells = cells;
            maxsize = cells.size()/2 + (cells.size()%2 == 1 ? 1 : 0);

            for (Cell c : cells) {
                if (c.is(type)) ++actual;
                if (!c.isDone() && c.canBe(type)) ++possible;
            }
        }
    }



    @Override public LogicStatus apply(Board thing) {
        int mincount = 0;
        int maxcount = 0;


        // we can assume that the points in p are in order
        // naive solution is count actual and possible across the whole row
        // better solution:  every block of cells of size k separated by blanks can have no more than ceil(k/2) cells of a given polarity

        List<List<Cell>> blocks = makeBlocks(thing);
        if (blocks == null) return LogicStatus.CONTRADICTION;

        List<BlockStatus> statuses = new ArrayList<>();

        int blockmax = 0;
        for (List<Cell> block : makeBlocks(thing)) {
            BlockStatus bs = new BlockStatus(thing,block);
            statuses.add(bs);
 //           System.out.println("BlockStatus: " + bs.possible + " " + bs.actual + " " + bs.maxsize);
            if (bs.actual > bs.maxsize) return LogicStatus.CONTRADICTION;
            int lmin = bs.actual;
            int lmax = bs.actual + bs.possible;
            if (lmax > bs.maxsize) lmax = bs.maxsize;
            mincount += lmin;
            maxcount += lmax;
            blockmax += bs.maxsize;
        }

 //       System.out.println("min : " + mincount + " goal: " + count + " max: " + maxcount);
        if (count < mincount) return LogicStatus.CONTRADICTION;
        if (count > maxcount) return LogicStatus.CONTRADICTION;

        if (count == mincount) {
            LogicStatus result = LogicStatus.STYMIED;
            for (Point p : points) {
                Cell c = thing.getCell(p.x,p.y);
                if (c.is(type)) continue;
                if (c.canBe(type)) {
                    c.clear(type);
                    result = LogicStatus.LOGICED;
                }
            }
            if (result == LogicStatus.LOGICED) return result;
        }

        // if the required number of things is equal to the sum of the maximum # of cells possible for each block,
        // then that means that every block must have exactly its max # of cells (if any were to have less,
        // that would mean that at least one would have to have more, which is impossible)
        // All blocks with an odd number of cells have only one choice:  All odd numbered cells (counting from 1)
        // must be our type, and all even numbered cells _must not_ be our type.

        if (count == blockmax) {
            LogicStatus result = LogicStatus.STYMIED;
            for (BlockStatus bs : statuses) {
                if (bs.cells.size() % 2 == 0) continue;
                boolean isodd = true;
                for (Cell c : bs.cells) {
                    if (isodd) {
                        if (!c.canBe(type)) return LogicStatus.CONTRADICTION;
                        if (!c.is(type)) {
                            c.set(type);
                            result = LogicStatus.LOGICED;
                        }
                    } else {
                        if (c.is(type)) return LogicStatus.CONTRADICTION;
                        if (c.canBe(type)) {
                            c.clear(type);
                            result = LogicStatus.LOGICED;
                        }
                    }
                    isodd = !isodd;
                }
            }
            if (result == LogicStatus.LOGICED) return result;
        }

        return LogicStatus.STYMIED;
    }

    @Override public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("LineLogicStep on " + type + " aiming for " + count + " points, containing cells:");
        for (Point p : points) { sb.append(" " + p.toString()); }
        return sb.toString();
    }
}
