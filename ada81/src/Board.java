import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.lambda.CellLambda;
import grid.lambda.LambdaInteger;
import grid.lambda.XYLambda;
import grid.logic.flatten.FlattenSolvable;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.StandardFlattenSolvable;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Board implements StandardFlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Deep CellContainer<Cell> cells;
    @Shallow CellContainer<Integer> regions;
    @Shallow List<List<Point>> regionpairs = new ArrayList<>();
    @Shallow CellContainer<Integer> splay;
    @Shallow List<Point> splayedlist;


    public Board(String fname) {
        gfr = new GridFileReader(fname);
        cells = new CellContainer<Cell>(getWidth(),getHeight(),
                (x,y)->new Cell(),
                (x,y,r)->new Cell(r)
        );


        List<Point> origlist = new ArrayList<>();
        splay = new CellContainer<Integer>(getWidth(),getHeight(), (x,y)->{
            origlist.add(new Point(x,y));
            return rowClueIf(0,y) + rowClueIf(getWidth()-1,y) + colClueIf(x,0) + colClueIf(x,getHeight()-1);
        });

        splayedlist = origlist.stream().sorted((b,a)->Integer.compare(splay.getCell(a.x,a.y),splay.getCell(b.x,b.y))).collect(Collectors.toList());
        System.out.println("list head: " + splay.getCell(splayedlist.get(0).x,splayedlist.get(0).y));



        regions = new CellContainer<Integer>(getWidth(),getHeight(),(x,y)->-1);
        LambdaInteger ctr = new LambdaInteger(1);
        forEachCell((x,y)-> {
           char dc = gfr.getBlock("DOMAINS")[x][y].charAt(0);
           if (dc != 'E' && dc != 'S') return;

           Direction d = Direction.fromShort(""+dc);
           int rid = ctr.get();
           ctr.inc();

           if (regions.getCell(x,y) != -1) throw new RuntimeException("another region overlaps region source " + x + "  " + y);
           regions.setCell(x,y,rid);
           Point ap = d.delta(x,y,1);
           if (!inBounds(ap.x,ap.y)) throw new RuntimeException("region leaves board at " + x + " " + y);
           if (regions.getCell(ap.x,ap.y) != -1) throw new RuntimeException("another region overlaps region target " + x + " " + y);
           regions.setCell(ap.x,ap.y,rid);

           List<Point> pair = new ArrayList<>();
           pair.add(new Point(x,y));
           pair.add(ap);
           regionpairs.add(pair);

        });

        forEachCell((x,y)-> {
            if (regions.getCell(x,y) == -1) throw new RuntimeException("Region Set does not cover board at " + x + " " + y);
        });




    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public boolean inBounds(int x,int y) { return gfr.inBounds(x,y); }
    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl);}
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public boolean hasColClue(int x,int y) { return gfr.getBlock("COLCLUES")[x][y].charAt(0) != '.'; }
    public int getColClue(int x,int y) { return Integer.parseInt(gfr.getBlock("COLCLUES")[x][y]); }
    public boolean hasRowClue(int x,int y) { return gfr.getBlock("ROWCLUES")[x][y].charAt(0) != '.'; }
    public int getRowClue(int x,int y) { return Integer.parseInt(gfr.getBlock("ROWCLUES")[x][y]); }
    public int rowClueIf(int x,int y) { return hasRowClue(x,y) ? getRowClue(x,y) : 0; }
    public int colClueIf(int x,int y) { return hasColClue(x,y) ? getColClue(x,y) : 0; }
    public int getRegionId(int x,int y) { return regions.getCell(x,y); }
    public Cell getCell(int x,int y) { return cells.getCell(x,y); }
    public List<List<Point>> getRegionPairs() { return regionpairs; }
    public int getSplay(int x,int y) { return splay.getCell(x,y); }


    ;
    private static class MyMove {
        int x;
        int y;
        MoveType type;
        boolean isSet;
        public MyMove(int x,int y,MoveType type,boolean isSet) { this.x = x; this.y = y; this.type = type; this.isSet = isSet; }
        public boolean applyMove(Board b) {
            Cell cell = b.getCell(x,y);
            if (isSet) {
                switch(type) {
                    case POSITIVE: if (!cell.canBePositive()) return false;   cell.setPositive(); return true;
                    case NEGATIVE: if (!cell.canBeNegative()) return false; cell.setNegative(); return true;
                    case BLANK: if (!cell.canBeBlank()) return false; cell.setBlank(); return true;
                    default: throw new RuntimeException("Bwa?");
                }
            } else {
                switch(type) {
                    case POSITIVE: cell.clearPositive(); return true;
                    case NEGATIVE: cell.clearNegative(); return true;
                    case BLANK: cell.clearBlank(); return true;
                    default: throw new RuntimeException("Bweh?");
                }
            }
        }
        @Override public String toString() { return "MyMove (" + x + "," + y + ") " + type + " " + isSet; }
    }

    @Override public boolean isComplete() { return CellLambda.terminatingForEachCell(getWidth(),getHeight(),(x,y)->cells.getCell(x,y).isDone()); }
    @Override public boolean applyMove(Object o) { return ((MyMove)o).applyMove(this); }

    @Override public FlattenSolvableTuple<Board> getOneTuple(int x, int y) {
        Cell c = getCell(x,y);
        if (c.isBroken() || c.isDone()) return null;
        FlattenSolvableTuple<Board> result = new FlattenSolvableTuple<>("Tuple (" + x + "," + y + ") " + c.canBePositive() + c.canBeNegative() + c.canBeBlank());
        if (c.canBePositive()) {
            Board b = new Board(this);
            MyMove mmpro = new MyMove(x,y,MoveType.POSITIVE,true);
            MyMove mmanti = new MyMove(x,y,MoveType.POSITIVE,false);
            mmpro.applyMove(b);
            result.addTuple(b,mmanti);
        }
        if (c.canBeNegative()) {
            Board b = new Board(this);
            MyMove mmpro = new MyMove(x,y,MoveType.NEGATIVE,true);
            MyMove mmanti = new MyMove(x,y,MoveType.NEGATIVE,false);
            mmpro.applyMove(b);
            result.addTuple(b,mmanti);
        }

        if (c.canBeBlank()) {
            Board b = new Board(this);
            MyMove mmpro = new MyMove(x,y,MoveType.BLANK,true);
            MyMove mmanti = new MyMove(x,y,MoveType.BLANK,false);
            mmpro.applyMove(b);
            result.addTuple(b,mmanti);
        }




        return result;
    }

    @Override public List<Board> guessAlternatives() {
        for (Point p : splayedlist) {
            FlattenSolvableTuple<Board> fst = getOneTuple(p.x,p.y);
            if (fst == null) continue;
            return fst.choices;
        }
        throw new RuntimeException("guessAlternatives called on solved board!");
    }

}

// lodestone
// magnetite