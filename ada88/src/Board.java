import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.lambda.CellLambda;
import grid.lambda.XYLambda;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.StandardFlattenSolvable;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;
import grid.puzzlebits.PointAdjacency;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Board implements StandardFlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Shallow Map<Character,List<Point>> regions = new HashMap<>();
    @Shallow Map<Character,Set<Character>> adjacentregions = new HashMap<>();
    @Deep CellContainer<Cell> cells;

    private void gocAddAdjacent(char r1,char r2) {
        if (!adjacentregions.containsKey(r1)) {
            adjacentregions.put(r1, new HashSet<Character>());
        }
        adjacentregions.get(r1).add(r2);
    }


    private void addRegionPair(int x,int y,Direction d) {
        Point op = d.delta(x,y,1);
        if (!inBounds(op)) return;
        char r1 = getRegionId(x,y);
        char r2 = getRegionId(op.x,op.y);
        if (r1 == r2) return;
        gocAddAdjacent(r1,r2);
        gocAddAdjacent(r2,r1);
    }

    public boolean regionsAdjacent(char rid1,char rid2) {
        if (rid1 == rid2) return true;
        return adjacentregions.containsKey(rid1) && adjacentregions.get(rid1).contains(rid2);
    }


    public Board(String fname) {
        gfr = new GridFileReader(fname);
        forEachCell((x,y)-> {
            char rid = getRegionId(x,y);
            if (rid == '.') throw new RuntimeException("period is illegal region id!");

            addRegionPair(x,y,Direction.EAST);
            addRegionPair(x,y,Direction.SOUTH);

            if (!regions.containsKey(rid)) {
                regions.put(rid,new ArrayList<>());
            }
            regions.get(rid).add(new Point(x,y));
        });

        for (char key : regions.keySet()) {
            if (!PointAdjacency.allAdjacent(regions.get(key),false)) throw new RuntimeException("Non-adjacent regions");
        }

        cells = new CellContainer<Cell>(getWidth(),getHeight(),
                (x,y)->{
                    Cell result = new Cell();
                    if (hasArrow(x,y)) result.set(getArrow(x,y));
                    return result;
                },
                (x,y,r)->new Cell(r)
        );
    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }


    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public boolean inBounds(int x,int y) { return gfr.inBounds(x,y); }
    public boolean inBounds(Point p) { return gfr.inBounds(p); }
    public char getRegionId(int x,int y) { return gfr.getBlock("REGIONS")[x][y].charAt(0); }
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.'; }
    public boolean hasArrow(int x,int y) {return gfr.getBlock("ARROWS")[x][y].charAt(0) != '.'; }
    public Direction getArrow(int x,int y) {return Direction.fromShort(gfr.getBlock("ARROWS")[x][y]); }
    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }

    public Cell getCell(int x,int y) { return cells.getCell(x,y);  }

    public Collection<Character> getRegionIds() { return regions.keySet(); }
    public Collection<Point> getRegionCells(char rid) { return regions.get(rid); }



    @Override public boolean isComplete() {
        return CellLambda.stream(getWidth(),getHeight()).allMatch(p->getCell(p.x,p.y).size() == 1);
    }

    private static class MyMove {
        int x;
        int y;
        Direction d;
        boolean doSet;
        public MyMove(int x,int y, Direction d,boolean doSet) {this.x = x; this.y = y; this.d = d; this.doSet = doSet; }
        public boolean applyMove(Board b) {
            Cell c = b.getCell(x,y);
            if (doSet) {
                if (!c.contains(d)) return false;
                c.set(d);
            } else {
                c.clear(d);
            }
            return true;
        }
    }

    @Override public boolean applyMove(Object o) { return ((MyMove)o).applyMove(this); }

    @Override public FlattenSolvableTuple<Board> getOneTuple(int x, int y) {
        Cell c = getCell(x,y);
        if (c.size() < 2) return null;
        FlattenSolvableTuple<Board> fst = new FlattenSolvableTuple<>();

        for (Direction d : c.getContents()) {
            Board b = new Board(this);
            MyMove pro = new MyMove(x,y,d,true);
            MyMove anti = new MyMove(x,y,d,false);
            pro.applyMove(b);
            fst.addTuple(b,anti);
        }
        return fst;
    }
}
