import grid.file.GridFileReader;
import grid.lambda.CellLambda;
import grid.logic.flatten.FlattenSolvable;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.StandardFlattenSolvable;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.PointAdjacency;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Board implements StandardFlattenSolvable<Board>
{
    private static String REGIONS = "REGIONS";
    private static String CELLCLUES = "CELLCLUES";
    private static String LEFTCLUES = "LEFTCLUES";
    private static String TOPCLUES = "TOPCLUES";
    private static String STARTINGCELLS = "STARTINGCELLS";

    private GridFileReader gfr;
    CellContainer<CellType> cells;
    CellContainer<Integer> weights;
    CellContainer<Clue> clues;
    Clue[] topclues;
    Clue[] leftclues;
    Map<Character,List<Point>> regions = new HashMap<>();

    private static int UNK_SIZE = 1;
    private static int UNK_CELL = 0;
    private int[] unknowns;

    public Board(String filename)
    {
        gfr = new GridFileReader(filename);

        unknowns = new int[UNK_SIZE];
        for (int i = 0 ; i < UNK_SIZE ; ++i) unknowns[i] = 0;

        cells = new CellContainer<CellType>(getWidth(),getHeight(),
                (x,y)-> {
                    if (gfr.hasBlock(STARTINGCELLS) && gfr.getBlock(STARTINGCELLS)[x][y].charAt(0) == 'W') return CellType.WHITE;
                    if (gfr.hasBlock(STARTINGCELLS) && gfr.getBlock(STARTINGCELLS)[x][y].charAt(0) == 'B') return CellType.BLACK;
                    ++unknowns[UNK_CELL];
                    return CellType.UNKNOWN;
                },
                (x,y,r) -> { return r; }
        );
        weights = new CellContainer<Integer>(getWidth(),getHeight(),(x,y) -> { return 1; },(x,y,r)-> {return new Integer(r.intValue());});

        if (gfr.hasBlock(TOPCLUES)) {
            topclues = new Clue[getWidth()];
            for (int x = 0; x < getWidth(); ++x) {
                String s = gfr.getBlock(TOPCLUES)[x][0];
                if (".".equals(s)) continue;
                topclues[x] = new Clue(s);
            }
        }

        if (gfr.hasBlock(LEFTCLUES)) {
            leftclues = new Clue[getHeight()];
            for (int y = 0; y < getHeight(); ++y) {
                String s = gfr.getBlock(LEFTCLUES)[0][y];
                if (".".equals(s)) continue;
                leftclues[y] = new Clue(s);
            }
        }

        if (gfr.hasBlock(CELLCLUES)) {
            clues = new CellContainer<Clue>(getWidth(), getHeight(),
                    (x, y) -> {
                        String s = gfr.getBlock(CELLCLUES)[x][y];
                        if (".".equals(s)) return null;
                        Clue nc = new Clue(s);

                        if (nc.ct == ClueType.CIRCLE) weights.setCell(x,y,nc.size);

                        return nc;
                    },
                    (x, y, r) -> {
                        return r;
                    });
        }


        CellLambda.forEachCell(getWidth(),getHeight(),(x,y)-> {
            char rid = getRegionId(x,y);
            if (rid == '.') return;
            if (!regions.containsKey(rid)) regions.put(rid,new ArrayList<Point>());
            regions.get(rid).add(new Point(x,y));
        });
        for (char rid : regions.keySet())
        {
            List<Point> lp = regions.get(rid);
            if (!PointAdjacency.allAdjacent(lp,false)) throw new RuntimeException("Non-adjacent cells for region " + rid);
        }
    }

    public Board(Board right)
    {
        gfr = right.gfr;
        cells = new CellContainer<CellType>(right.cells);
        weights = right.weights;
        topclues = right.topclues;
        leftclues = right.leftclues;
        clues = right.clues;
        regions = right.regions;

        unknowns = new int[UNK_SIZE];
        for (int i = 0 ; i < UNK_SIZE ; ++i) unknowns[i] = right.unknowns[i];

    }


    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public char getRegionId(int x, int y) { return gfr.hasBlock(REGIONS) ? gfr.getBlock(REGIONS)[x][y].charAt(0) : '.'; }
    public CellType getCell(int x,int y) { return cells.getCell(x,y); }
    public void setCell(int x,int y,CellType ct) { --unknowns[UNK_CELL]; cells.setCell(x,y,ct);}

    public Clue getClue(int x,int y) { return clues != null ? clues.getCell(x,y) : null;}
    public Clue getLeftClue(int y) { return leftclues != null ? leftclues[y] : null;}
    public Clue getTopClue(int x) { return topclues != null ? topclues[x] : null; }

    public Collection<Character> getRegionIds() { return regions.keySet(); }
    public Collection<Point> getCellsOfRegion(char rid) { return regions.get(rid); }

    public int getWeight(int x,int y) { return weights.getCell(x,y);}
    public boolean inBounds(int x,int y) { return gfr.inBounds(x,y); }

    @Override
    public boolean isComplete()
    {
        for (int i = 0 ; i < UNK_SIZE; ++i) if (unknowns[i] > 0) return false;
        return true;
    }

    private class MyMove
    {
        CellType ct;
        int x;
        int y;
        public MyMove(int x,int y,CellType ct) { this.x = x; this.y = y; this.ct = ct; }
    }


    @Override
    public boolean applyMove(Object o)
    {
        MyMove mm = (MyMove)o;
        if (this.getCell(mm.x,mm.y) != CellType.UNKNOWN) return this.getCell(mm.x,mm.y) == mm.ct;
        this.setCell(mm.x,mm.y,mm.ct);
        return true;
    }

    FlattenSolvableTuple<Board> makeTuple(MyMove mm1,MyMove mm2)
    {
        Board b1 = new Board(this);
        Board b2 = new Board(this);
        b1.applyMove(mm1);
        b2.applyMove(mm2);
        return new FlattenSolvableTuple<Board>(b1,mm1,b2,mm2);
    }


    @Override
    public FlattenSolvableTuple<Board> getOneTuple(int x, int y)
    {
        if (this.getCell(x,y) != CellType.UNKNOWN) return null;
        return makeTuple(new MyMove(x,y,CellType.WHITE),new MyMove(x,y,CellType.BLACK));
    }


}

