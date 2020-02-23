import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.lambda.CellLambda;
import grid.lambda.XYLambda;
import grid.logic.flatten.FlattenSolvable;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.StandardFlattenSolvable;
import grid.puzzlebits.CellContainer;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Board implements StandardFlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Deep CellContainer<Cell> cells;
    @Shallow Map<Character,List<Point>> regions = new HashMap<>();

    public Board(String fname) {
        gfr = new GridFileReader(fname);
        cells = new CellContainer<Cell>(getWidth(),getHeight(),
                (x,y)-> {
                    Cell nc = new Cell();
                    switch(getShape(x,y)) {
                        case 'T': nc.set(CellShape.TRIANGLE); break;
                        case 'C': nc.set(CellShape.CIRCLE); break;
                        case 'S': nc.set(CellShape.SQUARE); break;
                        case '.': break;
                        default: throw new RuntimeException("Unknown shape character " + getShape(x,y));
                    }
                    return nc;
                },
                (x,y,r)->new Cell(r)
                );

        forEachCell((x,y)-> {
            char c = getRegion(x,y);
            if (!regions.containsKey(c)) {
                regions.put(c,new ArrayList<Point>());
            }
            regions.get(c).add(new Point(x,y));
        });

    }

    public Board (Board right) {
        CopyCon.copy(this,right);
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public char getRegion(int x,int y) { return gfr.getBlock("REGIONS")[x][y].charAt(0); }
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public char getShape(int x,int y)  { return gfr.getBlock("SHAPES")[x][y].charAt(0); }
    public boolean onBoard(int x,int y) { return gfr.inBounds(x,y); }
    public boolean onBoard(Point p) { return gfr.inBounds(p); }
    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.'; }
    public Cell getCell(int x,int y) { return cells.getCell(x,y); }
    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }
    public List<Point> getCellsForRegion(char rid) { return regions.get(rid); }
    public Set<Character> getRegionIDs() { return regions.keySet(); }


    @Override public boolean isComplete() {
        return cells.terminatingForEachCell((x,y)->cells.getCell(x,y).isDone());
    }

    private static class MyMove {
        int x;
        int y;
        boolean doSet;
        CellShape cs;

        public MyMove(int x,int y,CellShape cs,boolean doSet) { this.x = x; this.y = y; this.cs = cs; this.doSet = doSet; }

        public boolean applyMove(Board b) {
            if (doSet) {
                if (!b.getCell(x,y).hasPossible(cs)) return false;
                b.getCell(x,y).set(cs);
                return true;
            } else {
                b.getCell(x,y).remove(cs);
                return true;
            }
        }

    }

    @Override public boolean applyMove(Object o) { return ((MyMove)o).applyMove(this); }

    @Override public FlattenSolvableTuple<Board> getOneTuple(int x, int y) {
        Cell c = getCell(x,y);
        if (c.isEmpty() || c.isDone()) return null;

        FlattenSolvableTuple<Board> fst = new FlattenSolvableTuple<>();
        for (CellShape cs : CellShape.values()) {
            if (!c.hasPossible(cs)) continue;
            MyMove move = new MyMove(x,y,cs,true);
            MyMove antimove = new MyMove(x,y,cs,false);
            Board nb = new Board(this);
            move.applyMove(nb);
            fst.addTuple(nb,antimove);
        }

        return fst;
    }
}
