import grid.logic.LogicStatus;
import grid.logic.LogicStep;

import javax.management.relation.RoleUnresolved;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class ClueLogicStep implements LogicStep<Board> {
    List<Integer> clues;
    List<Point> cells;
    public ClueLogicStep(List<Integer> clues, List<Point> cells) { this.clues = clues; this.cells = cells; }

    private class BlockSet {
        List<List<Point>> blocks = new ArrayList<>();
        private List<Point> cur = null;

        public boolean inBlock() { return cur != null; }


        public void startNewBlock() {
            if (inBlock()) throw new RuntimeException("Can't start a block while in one");
            cur = new ArrayList<>();
            blocks.add(cur);
        }

        public void addToBlock(Point p) {
            if (!inBlock()) startNewBlock();
            cur.add(p);
        }

        public void closeBlock() {
            cur = null;
        }
    }

    private BlockSet breakdown(Board thing, boolean unknownIsWall) {
        BlockSet bs = new BlockSet();
        for (Point p : cells) {
            Cell c = thing.getCell(p.x,p.y);
            if (c == null || c.isWall() || (unknownIsWall && c.canBeWall())) {
                bs.closeBlock();
            } else {
                bs.addToBlock(p);
            }
        }
        return bs;
    }




    @Override public LogicStatus apply(Board thing) {
        LogicStatus result = LogicStatus.STYMIED;
        BlockSet looseSet = breakdown(thing,false);

        int minblockcount = 0;
        int maxblockcount = 0;
        for(List<Point> list : looseSet.blocks) {
            minblockcount++;
            maxblockcount += (list.size()+1)/2;
        }

        if (minblockcount > clues.size()) return LogicStatus.CONTRADICTION;
        if (maxblockcount < clues.size()) return LogicStatus.CONTRADICTION;

        if (maxblockcount == clues.size()) {
            for(List<Point> list : looseSet.blocks) {
                if (list.size() % 2 == 1) {
                    for (int i = 1 ; i < list.size() ; i += 2) {
                        Point curpoint = list.get(i);
                        thing.getCell(curpoint.x,curpoint.y).makeWall();
                        result = LogicStatus.LOGICED;
                    }
                }
            }
        }

        if (result == LogicStatus.LOGICED) looseSet = breakdown(thing,false);
        if (looseSet.blocks.size() != clues.size()) return result;

        for (int bidx = 0 ; bidx < looseSet.blocks.size() ; ++bidx) {
            int csize = clues.get(bidx);
            List<Point> block = looseSet.blocks.get(bidx);
            if (csize == -1) continue;
            int bcount = 0;
            for (Point p : block) {
                Cell c = thing.getCell(p.x,p.y);
                if (!c.isComplete()) {
                    bcount = -1;
                    break;
                }
                bcount += c.getSingleNumber();
            }
            if (bcount == -1) continue;
            if (bcount != csize) return LogicStatus.CONTRADICTION;
        }

        return result;
    }
}
