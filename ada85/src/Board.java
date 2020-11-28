import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.lambda.BooleanXYLambda;
import grid.lambda.CellLambda;
import grid.lambda.XYLambda;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.StandardFlattenSolvable;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Board implements StandardFlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Deep CellContainer<CellContents> cells;
    @Shallow Map<Character,List<Point>> regions;

    public Board(String fname) {
        gfr = new GridFileReader(fname);
        regions = new HashMap<>();
        forEachCell((x,y)->{
            char rid = getRegionId(x,y);
            if (!regions.containsKey(rid)) regions.put(rid,new ArrayList<>());
            regions.get(rid).add(new Point(x,y));
        });

        cells = new CellContainer<CellContents>(getWidth(),getHeight(),
                (x,y)-> {
                    char rid = getRegionId(x,y);
                    int rcount = regions.get(rid).size();
                    CellContents cc = new CellContents(rcount);
                    if (hasClue(x,y)) {
                        cc.set(getClue(x,y));
                    }
                    return cc;
                },
                (x,y,r)->new CellContents(r)
                );

    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }

    public Collection<Character> getRegionIds() { return regions.keySet(); }
    public List<Point> getCellsForRegion(char rid) { return regions.get(rid); }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public boolean inBounds(int x,int y) { return gfr.inBounds(x,y); }
    public boolean inBounds(Point p) { return gfr.inBounds(p); }
    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }
    public boolean terminatingForEachCell(BooleanXYLambda bxyl) { return CellLambda.terminatingForEachCell(getWidth(),getHeight(),bxyl);}
    public char getRegionId(int x,int y) { return gfr.getBlock("REGIONS")[x][y].charAt(0); }
    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.'; }
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public boolean hasClue(int x,int y) { return gfr.getBlock("CLUES")[x][y].charAt(0) != '.'; }
    public int getClue(int x,int y) { return Integer.parseInt(gfr.getBlock("CLUES")[x][y]); }

    public CellContents getCell(int x,int y) { return cells.getCell(x,y); }

    public boolean sameRegion(int x,int y,Direction d) {
        Point op = d.delta(x,y,1);
        return getRegionId(x,y) == getRegionId(op.x,op.y);
    }

    private static class MyMove {
        int x;
        int y;
        boolean isSet;
        int number;

        public MyMove(int x,int y,int number,boolean isSet) { this.x = x; this.y = y; this.number = number; this.isSet = isSet; }
        public boolean applyMove(Board b) {
            if (isSet) {
                return b.getCell(x,y).set(number);
            } else {
                b.getCell(x,y).clear(number);
                return true;
            }
        }
    }


    @Override public boolean isComplete() { return terminatingForEachCell((x,y)->getCell(x,y).size() == 1);    }
    @Override public boolean applyMove(Object o) { return ((MyMove)o).applyMove(this); }

    @Override public FlattenSolvableTuple<Board> getOneTuple(int x, int y) {
        CellContents cc = getCell(x,y);
        if (cc.size() < 2) return null;
        FlattenSolvableTuple<Board> fst = new FlattenSolvableTuple<>();

        for (int i : cc.getPossibles()) {
            Board b = new Board(this);
            MyMove pro = new MyMove(x,y,i,true);
            MyMove anti = new MyMove(x,y,i,false);
            pro.applyMove(b);
            fst.addTuple(b,anti);
        }

        return fst;
    }

}
