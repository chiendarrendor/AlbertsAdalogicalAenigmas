import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.lambda.LambdaInteger;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.StandardFlattenSolvable;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class AdaBoard implements StandardFlattenSolvable<AdaBoard> {
    @Shallow GridFileReader gfr;
    @Deep CellContainer<CellType> cells;
    @Shallow int unknowns;

    public AdaBoard(String fname) {
        gfr = new GridFileReader(fname);

        LambdaInteger unk = new LambdaInteger(0);
        cells = new CellContainer<CellType>(getWidth(),getHeight(),(x,y)-> {
            if (isClue(x,y)) {
                return CellType.BLOCK;
            }
            unk.inc();
            return CellType.EMPTY;
        });
        unknowns = unk.get();
    }

    public AdaBoard(AdaBoard right) { CopyCon.copy(this,right); }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }

    public boolean isClue(int x,int y) { return !gfr.getBlock("CLUES")[x][y].equals("."); }
    public boolean isNumberedClue(int x,int y) { return Character.isDigit(gfr.getBlock("CLUES")[x][y].charAt(0)); }
    public int getClueNumber(int x,int y) { return Integer.parseInt(gfr.getBlock("CLUES")[x][y]);}
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0);}
    public boolean isLetter(int x,int y) { return getLetter(x,y) != '.'; }
    public CellType getCell(int x,int y) { return cells.getCell(x,y); }

    public void setCell(int x,int y,CellType ct,String why) {
        cells.setCell(x,y,ct);
        --unknowns;
 //       System.out.println("Cell (" + x + "," + y + ") set to " + ct + " because " + why);
    }

    public boolean onBoard(int x,int y) { return gfr.inBounds(x,y); }
    public boolean onBoard(Point p) { return gfr.inBounds(p); }

    public List<Point> getVisibleCells(int x,int y,boolean adjacentonly) {
        List<Point> result = new ArrayList<>();
        for (Direction d: Direction.orthogonals()) {
            int idx = 1;
            while(true) {
                Point np = d.delta(x,y,idx);
                if (!onBoard(np)) break;
                if (isClue(np.x,np.y)) break;
                if (adjacentonly && idx > 1) break;
                result.add(np);
                ++idx;
            }
        }
        return result;
    }


    public boolean isLit(int x,int y) {
        if (getCell(x,y) == CellType.BULB) return true;
        return getVisibleCells(x,y,false).stream().anyMatch(p->getCell(p.x,p.y)==CellType.BULB);
    }



    @Override public boolean isComplete() { return unknowns == 0; }

    private static class MyMove {
        int x;
        int y;
        boolean isbulb;
        public MyMove(int x,int y,boolean isbulb) { this.x = x; this.y = y; this.isbulb = isbulb; }
        public boolean applyMove(AdaBoard b) {
            CellType target = isbulb ? CellType.BULB : CellType.NOBULB;
            if (b.getCell(x,y) != CellType.EMPTY) return b.getCell(x,y) == target;
            b.setCell(x,y,target,"this is a guess");
            return true;
        }
    }

    @Override public boolean applyMove(Object o) { return ((MyMove)o).applyMove(this); }

    @Override public FlattenSolvableTuple<AdaBoard> getOneTuple(int x, int y) {
        if (getCell(x,y) != CellType.EMPTY) return null;
        AdaBoard b1 = new AdaBoard(this);
        AdaBoard b2 = new AdaBoard(this);
        MyMove mm1 = new MyMove(x,y,true);
        MyMove mm2 = new MyMove(x,y,false);
        mm1.applyMove(b1);
        mm2.applyMove(b2);

        return new FlattenSolvableTuple<AdaBoard>(b1,mm1,b2,mm2);
    }


}
