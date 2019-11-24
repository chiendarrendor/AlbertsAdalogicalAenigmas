import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.logic.flatten.FlattenSolvable;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Board implements FlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Deep TileSet alltiles;
    @Deep CellContainer<TileSet> cells;

    private static final Direction[] workdirs = { Direction.EAST,Direction.SOUTH };

    public Board(String fname) {
        gfr = new GridFileReader(fname);
        cells = new CellContainer<TileSet>(getWidth(),getHeight(),(x,y)->new TileSet(),(x,y,r)->new TileSet(r));

        alltiles = new TileSet();

        for (int y = 0 ; y < getHeight() ; ++y) {
            for (int x = 0 ; x < getWidth() ; ++x) {
                Point p = new Point(x,y);
                boolean seenone = false;
                for (Direction d : workdirs ) {
                    for (int size = 1 ; size <= 4 ; ++size) {
                        if (size == 1) {
                            if (seenone == true) continue;
                            seenone = true;
                        }


                        Tile t = new Tile(p,size,d);
                        if (onBoard(t)) addTile(t);
                    }
                }
            }
        }
    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0);}
    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.'; }
    public boolean hasNumber(int x,int y) { return gfr.getBlock("NUMBERS")[x][y].charAt(0) != '.'; }
    public int getNumber(int x,int y) { return Integer.parseInt(gfr.getBlock("NUMBERS")[x][y]); }
    public boolean onBoard(Point p ) { return gfr.inBounds(p); }
    public TileSet getCell(int x,int y) { return cells.getCell(x,y); }


    private boolean onBoard(Tile t) {  return t.cells.stream().allMatch(p->onBoard(p));  }


    private void addTile(Tile t) {
        alltiles.add(t);
        t.cells.stream().forEach(p->getCell(p.x,p.y).add(t));
    }

    // return -1 on bad tile
    // returns the number of other tiles cleaned otherwise.
    public int set(Tile t) {
        for (Point p : t.cells) {
            if (!getCell(p.x,p.y).set.contains(t)) return -1;
        }
        Set<Tile> doomed = new HashSet<>();
        for (Point p : t.cells) {
            for (Tile dt : getCell(p.x,p.y).set) {
                doomed.add(dt);
            }
        }
        doomed.remove(t);
        for (Tile dt : doomed) clear(dt);

        return doomed.size();
    }

    public boolean clear(Tile t) {
//        if (!alltiles.set.contains(t)) return false;
        alltiles.set.remove(t);
        for (Point p : t.cells) {
            getCell(p.x,p.y).set.remove(t);
        }
        return true;
    }



    @Override public boolean isComplete() {
        for (int y = 0 ; y < getHeight() ; ++y) {
            for (int x = 0 ; x < getWidth() ; ++x) {
                if (getCell(x,y).set.size() != 1) return false;
            }
        }
        return true;
    }

    public boolean isNumbered(Tile next) {
        return next.cells.stream().anyMatch(p->hasNumber(p.x,p.y));
    }

    private static class MyMove {
        Tile t;
        boolean doSet;
        public MyMove(Tile t,boolean doSet) { this.t = t; this.doSet = doSet; }
        public boolean applyMove(Board b) {
            if (doSet) {
                return b.set(t) >= 0;
            } else {
                return b.clear(t);
            }
        }
        public String toString() { return "MyMove: " + t + ": " + doSet; }
    }


    @Override public boolean applyMove(Object o) { return ((MyMove)o).applyMove(this); }


    private List<FlattenSolvableTuple<Board>> getTupleList(boolean getall) {
        List<FlattenSolvableTuple<Board>> result = new ArrayList<>();

        for (Tile t : alltiles.set) {
            Board b1 = new Board(this);
            Board b2 = new Board( this);
            MyMove mm1 = new MyMove(t,true);
            MyMove mm2 = new MyMove(t,false);
            mm1.applyMove(b1);
            mm2.applyMove(b2);

            result.add(new FlattenSolvableTuple<Board>(b1,mm1,b2,mm2));
            if (!getall) break;
        }
        return result;
    }



    @Override public List<FlattenSolvableTuple<Board>> getSuccessorTuples() {
        return getTupleList(true);
    }

    @Override public List<Board> guessAlternatives() {
        List<FlattenSolvableTuple<Board>> alts = getTupleList(false);
        return alts.get(0).choices;
    }
}
