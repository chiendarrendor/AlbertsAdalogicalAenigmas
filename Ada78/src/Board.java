import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Ignore;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.lambda.CellLambda;
import grid.lambda.LambdaInteger;
import grid.lambda.XYLambda;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.StandardFlattenSolvable;
import grid.puzzlebits.CellContainer;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Board implements StandardFlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Shallow Map<Character,Integer> regionLabels = new HashMap<>();
    @Shallow Map<Character,List<Point>> regionCells = new HashMap<>();
    @Deep CellContainer<CellState> cells;
    @Shallow int unknowns;

    public Board(String fname) {
        gfr = new GridFileReader(fname);

        LambdaInteger unk = new LambdaInteger(0);
        cells = new CellContainer<CellState>(getWidth(),getHeight(),(x,y)->{
           char rid = getRegionId(x,y);
           if (!regionCells.containsKey(rid)) {
               regionCells.put(rid, new ArrayList<>());
           }

           regionCells.get(rid).add(new Point(x,y));

           if (hasLabel(x,y)) {
               if (regionLabels.containsKey(rid) && regionLabels.get(rid) > 0 && regionLabels.get(rid) != getLabel(x,y))
                   throw new RuntimeException("second non-matching Label in Region " + rid);
               regionLabels.put(rid,getLabel(x,y));
               return CellState.LABEL;
           } else {
               if (!regionLabels.containsKey(rid)) regionLabels.put(rid, -1);
               unk.inc();
               return CellState.UNKNOWN;
           }
        });
        unknowns = unk.get();
    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public char getRegionId(int x,int y) { return gfr.getBlock("REGIONS")[x][y].charAt(0); }
    public char getRegionId(Point p) { return getRegionId(p.x,p.y); }
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.'; }
    public boolean hasLabel(int x,int y) { return gfr.getBlock("LABELS")[x][y].charAt(0) != '.'; }
    public int getLabel(int x,int y) { return Integer.parseInt(gfr.getBlock("LABELS")[x][y]); }
    public boolean onBoard(int x,int y) { return gfr.inBounds(x,y); }
    public boolean onBoard(Point p) { return gfr.inBounds(p); }
    public int getRegionLabel(char rid) { return regionLabels.get(rid); }
    public Set<Character> getRegionIds() { return regionLabels.keySet(); }
    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }

    public CellState getCell(int x,int y) { return cells.getCell(x,y); }
    public void setCell(int x,int y,CellState cs) {
        cells.setCell(x,y,cs);
        unknowns--;
    }

    public static class CountHolder {
        int barriercount;
        int labelcount;
        List<Point> unknowns = new ArrayList<>();
    }

    public CountHolder getRegionCounts(char rid) {
        CountHolder result = new CountHolder();
        for (Point p : regionCells.get(rid)) {
            switch(getCell(p.x,p.y)) {
                case LABEL: ++ result.labelcount; break;
                case BARRIER: ++result.barriercount; break;
                case UNKNOWN: result.unknowns.add(p); break;
            }
        }
        return result;
    }

    public int liveLabelCount(char rid) {
        if (regionLabels.get(rid) > 0) return regionLabels.get(rid);
        CountHolder ch = getRegionCounts(rid);
        if (ch.unknowns.size() > 0) return -1;
        return ch.labelcount;
    }



    @Override public boolean isComplete() { return unknowns == 0; }

    private static class MyMove {
        int x;
        int y;
        CellState cs;
        public MyMove(int x,int y, CellState cs) { this.x = x; this.y = y; this.cs = cs; }
        public boolean applyMove(Board b) {
            if (b.getCell(x,y) != CellState.UNKNOWN) return cs == b.getCell(x,y);
            b.setCell(x,y,cs);
            return true;
        }
    }


    @Override public boolean applyMove(Object o) { return ((MyMove)o).applyMove(this); }

    @Override public FlattenSolvableTuple<Board> getOneTuple(int x, int y) {
        if (getCell(x,y) != CellState.UNKNOWN) return null;
        Board b1 = new Board(this);
        Board b2 = new Board(this);
        MyMove mm1 = new MyMove(x,y,CellState.BARRIER);
        MyMove mm2 = new MyMove(x,y,CellState.LABEL);
        mm1.applyMove(b1);
        mm2.applyMove(b2);
        return new FlattenSolvableTuple<Board>(b1,mm1,b2,mm2);
    }
}
