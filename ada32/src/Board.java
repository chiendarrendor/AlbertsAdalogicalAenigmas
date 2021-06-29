
import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.lambda.BooleanXYLambda;
import grid.lambda.CellLambda;
import grid.lambda.XYLambda;
import grid.logic.flatten.FlattenSolvable;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.StandardFlattenSolvable;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Board implements StandardFlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Deep CellContainer<NumCell> cells;

    public Board(String fname) {
        gfr = new GridFileReader(fname);
        cells = new CellContainer<NumCell>(getWidth(),getHeight(),
                (x,y)->new NumCell(hasBlock(x,y)),
                (x,y,r)->new NumCell(r)
        );
    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.'; }
    public boolean inBounds(int x,int y) { return gfr.inBounds(x,y) && !hasBlock(x,y); }
    public boolean terminatingForEachCell(BooleanXYLambda bxyl) { return CellLambda.terminatingForEachCell(getWidth(),getHeight(),bxyl); }
    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }

    public NumCell getCell(int x,int y) { return cells.getCell(x,y); }

    public boolean hasBlock(int x,int y) { return !gfr.getBlock("ARROWS")[x][y].equals("."); }
    public boolean hasClue(int x,int y) {
        String s = gfr.getBlock("ARROWS")[x][y];
        if (s.length() != 2) return false;
        if ("Vv><^".indexOf(s.charAt(0)) == -1) return false;
        if (!Character.isDigit(s.charAt(1))) return false;
        int cv = Integer.parseInt(s.substring(1));
        if (cv < 0 || cv > 5) return false;
        return true;
    }

    public Direction getClueDirection(int x, int y) {
        switch(gfr.getBlock("ARROWS")[x][y].charAt(0)) {
            case '^': return Direction.NORTH;
            case 'V':
            case 'v': return Direction.SOUTH;
            case '<': return Direction.WEST;
            case '>': return Direction.EAST;
            default: throw new RuntimeException("Shouldn't happen");
        }
    }

    public int getClueNumber(int x,int y) {
        return Integer.parseInt(gfr.getBlock("ARROWS")[x][y].substring(1));
    }

    private static class MyMove {
        int x;
        int y;
        int num;
        boolean doSet;
        public MyMove(int x,int y,int num,boolean doSet) { this.x = x; this.y = y; this.num = num; this.doSet = doSet; }
        public boolean applyMove(Board b) {
            NumCell ns = b.getCell(x,y);
            if (doSet) {
                if (!ns.doesContain(num)) return false;
                ns.set(num);
            } else {
                ns.remove(num);
            }
            return true;
        }
    }

    @Override public boolean isComplete() { return terminatingForEachCell((x,y)->getCell(x,y).isDone()); }
    @Override public boolean applyMove(Object o) { return ((MyMove)o).applyMove(this); }

    @Override public FlattenSolvableTuple<Board> getOneTuple(int x, int y) {
        NumCell ns = getCell(x,y);
        if (ns.isDone() || ns.isBroken()) return null;
        FlattenSolvableTuple<Board> fst = new FlattenSolvableTuple<>();
        for (int i : ns.getPossibles()) {
            Board b = new Board(this);
            MyMove mmpro = new MyMove(x,y,i,true);
            MyMove mmanti = new MyMove(x,y,i,false);
            mmpro.applyMove(b);
            fst.addTuple(b,mmanti);
        }

        return fst;
    }

    private static class SortPair implements Comparable<SortPair> {
        Point p;
        int val;
        public SortPair(Point p,int val) { this.p = p ; this.val = val; }

        private static final Point center = new Point(10,6);
        private int distanceFromCenter() {
            return Math.abs(p.x - center.x) + Math.abs(p.y - center.y);
        }

        public int compareTo(SortPair right) {
            int c1 = Integer.compare(val, right.val);
            if (c1 != 0) return c1;
            return Integer.compare(right.distanceFromCenter(), distanceFromCenter());
        }
    }



    @Override public List<FlattenSolvableTuple<Board>> getTupleList(boolean onlyone) {
        List<SortPair> activePoints = new ArrayList<>();
        forEachCell((x,y)-> {
            NumCell ns = getCell(x,y);
            if (ns.getPossibles().size() < 2) return;
            activePoints.add(new SortPair(new Point(x,y),ns.getPossibles().size()));
        });
        Collections.sort(activePoints);

        // calls 'List<FlattenSolvableTuple<Board> getTuplesForCell(int x, int y) on every cell that can have choices
        //  and aggregates them into a single list (stopping after the first one if onlyone is true
        List<FlattenSolvableTuple<Board>> result = new ArrayList<>();
        for(SortPair sp : activePoints) {
            List<FlattenSolvableTuple<Board>> fstlist = getTuplesForCell(sp.p.x,sp.p.y);
            if (fstlist == null) continue;
            if (fstlist.size() == 0) continue;
            result.addAll(fstlist);
            if (onlyone) break;
        }

        return result;
    }
}
