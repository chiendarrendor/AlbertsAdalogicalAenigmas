import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.lambda.CellLambda;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.StandardFlattenSolvable;
import grid.puzzlebits.CellContainer;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Board implements StandardFlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Deep CellContainer<Cell> cells;
    @Shallow Map<Character,Arrow> arrows = new HashMap<>();
    @Shallow Map<Character,Cage> cages = new HashMap<>();
    @Shallow Map<Point,Cage> cagerefs = new HashMap<>();

    @Shallow List<Point> searchOrder = new ArrayList<>();
    @Shallow Set<Point> placed = new HashSet<>();

    private void setSearch(Point p) {
        if (placed.contains(p)) return;
        searchOrder.add(p);
        placed.add(p);
    }

    private void completeSearch() {
        for (int y = 0 ; y < getHeight() ; ++y) {
            for (int x = 0 ; x < getWidth() ; ++x) {
                Point np = new Point(x,y);
                if (placed.contains(np)) continue;
                searchOrder.add(np);
            }
        }
    }




    public Board(String fname) {
        gfr = new GridFileReader(fname);

        Pattern cagePattern = Pattern.compile("^(\\p{Alpha})(\\d+)?$");


        cells = new CellContainer<Cell>(getWidth(),getHeight(),
                (x,y)-> {
                    if (hasArrow(x,y)) {
                        char rawarrow = getArrow(x,y);
                        char arrowid = Character.toLowerCase(rawarrow);
                        if (!arrows.containsKey(arrowid)) arrows.put(arrowid,new Arrow(arrowid));
                        arrows.get(arrowid).add(rawarrow,x,y);
                    }

                    if (hasCage(x,y)) {
                        String cageinfo = getCage(x,y);
                        Matcher m = cagePattern.matcher(cageinfo);

                        if (m == null || !m.matches()) throw new RuntimeException("Cage descriptor doesn't match");

                        char c = m.group(1).charAt(0);
                        if (!cages.containsKey(c)) cages.put(c,new Cage(c));
                        Cage cage = cages.get(c);
                        cage.add(x,y);
                        if (m.group(2) != null) {
                            cage.addCount(Integer.parseInt(m.group(2)));
                        }
                        cagerefs.put(new Point(x,y),cage);
                    }


                    return new Cell();
                },
                (x,y,r)-> new Cell(r));




        for (Arrow arrow : arrows.values()) {
            if (arrow.head == null) throw new RuntimeException("Headless Arrow");
            setSearch(arrow.head);
            for (Point p : arrow.body) setSearch(p);
        }

        for (Cage cage : cages.values()) {
            if (cage.size == -1) throw new RuntimeException("Numberless Cage");
            for (Point p : cage.cells) setSearch(p);
        }

        completeSearch();


    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public Cell getCell(int x, int y) { return cells.getCell(x,y); }
    public boolean hasArrow(int x, int y) { return getArrow(x,y) != '.'; }
    public char getArrow(int x, int y) { return gfr.getBlock("ARROWS")[x][y].charAt(0); }
    public String getCage(int x, int y) { return gfr.getBlock("CAGES")[x][y]; }
    public boolean hasCage(int x, int y) { return !getCage(x,y).equals("."); }

    public Cage getCageInfo(int x,int y) { return cagerefs.get(new Point(x,y)); }

    public Arrow getArrowObject(char c) { return arrows.get(c); }
    public Cage getCageObject(char c) { return cages.get(c); }

    public Collection<Arrow> getArrowList() { return arrows.values(); }
    public Collection<Cage> getCageList() { return cages.values(); }


    @Override public boolean isComplete() {
        return CellLambda.stream(getWidth(),getHeight()).allMatch(p->getCell(p.x,p.y).isDone());
    }


    private static class MyMove {
        int x;
        int y;
        int v;
        boolean isSet;
        public MyMove(int x,int y,int v, boolean isSet) { this.x = x; this.y = y; this.v = v; this.isSet = isSet; }
        public boolean applyMove(Board b) {
            Cell c = b.getCell(x,y);
            if (isSet) {
                if (!c.has(v)) return false;
                c.set(v);
            } else {
                c.clear(v);
            }
            return true;
        }
    }

    @Override public boolean applyMove(Object o) { return ((MyMove)o).applyMove(this); }

    @Override public FlattenSolvableTuple<Board> getOneTuple(int x, int y) {
        Cell c = getCell(x,y);
        if (c.isDone() || !c.isValid()) return null;
        FlattenSolvableTuple<Board> fst = new FlattenSolvableTuple<>();
        for (int v : c.contents()) {
            Board b = new Board(this);
            MyMove pro = new MyMove(x,y,v,true);
            MyMove anti = new MyMove(x,y,v,false);
            pro.applyMove(b);
            fst.addTuple(b,anti);
        }
        return fst;
    }


    @Override public List<FlattenSolvableTuple<Board>> getTupleList(boolean onlyone) {
        Vector<FlattenSolvableTuple<Board>> result = new Vector<>();

        for (Point p : searchOrder) {
            List<FlattenSolvableTuple<Board>> fstlist = getTuplesForCell(p.x, p.y);
            if (fstlist == null) continue;
            if (fstlist.size() == 0) continue;
            result.addAll(fstlist);
            if (onlyone) return result;
        }
        return result;
    }

}
