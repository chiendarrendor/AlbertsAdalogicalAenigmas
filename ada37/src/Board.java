import com.sun.org.apache.xpath.internal.operations.Bool;
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

import java.util.Collection;

public class Board implements StandardFlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Deep CellContainer<CellData> cells;

    public Board(String fname) {
        gfr = new GridFileReader(fname);
        cells = new CellContainer<CellData>(getWidth(),getHeight(),
                (x,y)->{
                    CellData cd = new CellData();
                    if (hasClue(x,y)) cd.set(getClue(x,y));
                    return cd;
                },
                (x,y,r)->new CellData(r));
    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }


    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public boolean inBounds(int x,int y) { return gfr.inBounds(x,y); }
    public String getName() { return gfr.getVar("NAME"); }
    public boolean booleanForEachCell(BooleanXYLambda bxyl) { return CellLambda.terminatingForEachCell(getWidth(),getHeight(),bxyl); }
    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }
    public boolean hasClue(int x,int y) { return !".".equals(gfr.getBlock("CLUES")[x][y]); }
    public int getClue(int x,int y) { return Integer.parseInt(gfr.getBlock("CLUES")[x][y]); }
    public SolutionType getSolutionType() { return SolutionType.valueOf(gfr.getVar("SOLUTIONTYPE"));}
    public String getSolution() { return gfr.getVar("SOLUTION"); }
    public boolean isShaded(int x, int y) { return gfr.getBlock("SHADES")[x][y].charAt(0) == '@'; }
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.'; }


    public CellData getCellData(int x,int y) { return cells.getCell(x,y); }

    @Override public boolean isComplete() {
        return booleanForEachCell((x,y)->getCellData(x,y).isComplete());
    }

    private static class MyMove {
        int x;
        int y;
        int v;
        boolean isSet;
        public MyMove(int x,int y,int v,boolean isSet) { this.x = x; this.y = y; this.v = v; this.isSet = isSet; }
        public boolean applyMove(Board b) {
            CellData cd = b.getCellData(x,y);
            if (isSet) {
                if (!cd.has(v)) return false;
                cd.set(v);
                return true;
            } else {
                cd.clear(v);
                return true;
            }
        }

    }


    @Override public boolean applyMove(Object o) { return ((MyMove)o).applyMove(this); }

    @Override public FlattenSolvableTuple<Board> getOneTuple(int x, int y) {
        Collection<Integer> possibles = getCellData(x,y).values();
        if (possibles.size() < 2) return null;
        FlattenSolvableTuple<Board> fst = new FlattenSolvableTuple<Board>();

        for(int v : possibles) {
            Board nb = new Board(this);
            MyMove pro = new MyMove(x,y,v,true);
            MyMove anti = new MyMove(x,y,v,false);
            pro.applyMove(nb);
            fst.addTuple(nb,anti);
        }

        return fst;
    }







}
