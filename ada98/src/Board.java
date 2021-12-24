import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.lambda.BooleanXYLambda;
import grid.lambda.CellLambda;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.StandardFlattenSolvable;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Board implements StandardFlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Deep CellContainer<CellData> cells;
    @Shallow Set<Integer> solutionNumbers = new HashSet<>();

    public Board(String fname) {
        gfr = new GridFileReader(fname);
        cells = new CellContainer<CellData>(getWidth(),getHeight(),
                (x,y)->{
                    if (!inBounds(x,y)) return null;
                    if (isShaded(x,y)) return null;
                    CellData cd = new CellData(x,y,getMinNumber(),getMaxNumber());
                    if (hasClue(x,y)) {
                        cd.set(getClue(x,y));
                    }
                    return cd;
                },
                (x,y,r)->  r == null ? null : new CellData(r)

        );

        int[] solvals = gfr.getIntArrayVar("SOLUTIONNUMBERS");
        for (int sv : solvals) solutionNumbers.add(sv);


    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public String getGridName() { return gfr.getVar("GRIDNAME"); }
    public int getMinNumber() { return gfr.getIntVar("MINNUM");}
    public int getMaxNumber() { return gfr.getIntVar("MAXNUM");}
    public boolean inBounds(int x,int y) { return gfr.inBounds(x,y) && gfr.getBlock("SHADES")[x][y].charAt(0) != '!'; }
    public boolean isShaded(int x, int y) { return gfr.getBlock("SHADES")[x][y].charAt(0) == '@'; }
    public boolean hasClue(int x,int y) { return !".".equals(gfr.getBlock("CLUES")[x][y]); }
    public int getClue(int x,int y) { return Integer.parseInt(gfr.getBlock("CLUES")[x][y]); }
    public boolean isSolutionNumber(int v) { return solutionNumbers.contains(v); }
    public String getSolutionClue() { return gfr.getVar("SOLUTIONCLUE");}
    public String getSolution() { return gfr.getVar("SOLUTION"); }
    public CellData getCellData(int x,int y) { return cells.getCell(x,y); }



    public boolean booleanForEachCell(BooleanXYLambda bxyl) { return CellLambda.terminatingForEachCell(getWidth(),getHeight(),bxyl); }

    public int getBoxCount() { return gfr.getIntVar("NUMBOXES");}
    public int getBoxSize() { return gfr.getIntVar("BOXSIZE");}


    public List<Point> getBoxList(int xstart, int ystart,Direction d) {
        List<Point> result = new ArrayList<>();
        for (int i = 0 ; i < getBoxSize() ; ++i) {
            Point np = d.delta(xstart,ystart,i);
            if (isShaded(np.x,np.y)) continue;
            result.add(np);
        }


        return result;
    }

    public List<Point> getBoxRow(int boxid, int rownum) {
        int[] boxul = gfr.getIntArrayVar("BOX" + boxid);
        return getBoxList(boxul[0],boxul[1]+rownum,Direction.EAST);
    }

    public List<Point> getBoxColumn(int boxid, int colnum) {
        int[] boxul = gfr.getIntArrayVar("BOX" + boxid);
        return getBoxList(boxul[0]+colnum,boxul[1],Direction.SOUTH);
    }



    @Override public boolean isComplete() {
        return booleanForEachCell((x, y) -> {
            CellData cd = getCellData(x, y);
            if (cd == null) return true;
            return cd.isComplete();
        });
    }

    private static class MyMove {
        int x;
        int y;
        int num;
        boolean isSet;
        public MyMove(int x,int y,int num,boolean isSet) {this.x = x; this.y = y; this.num = num; this.isSet = isSet; }
        public boolean applyMove(Board b) {
            CellData cd = b.getCellData(x,y);
            if (isSet) {
                if (!cd.has(num)) return false;
                cd.set(num);
                return true;
            } else {
                cd.clear(num);
                return true;
            }
        }
    }


    @Override public boolean applyMove(Object o) { return ((MyMove)o).applyMove(this); }

    @Override public FlattenSolvableTuple<Board> getOneTuple(int x, int y) {
        CellData cd = getCellData(x,y);
        if (cd == null) return null;
        Collection<Integer> possibles = cd.possibles();
        if (possibles.size() < 2) return null;
        FlattenSolvableTuple<Board> fst = new FlattenSolvableTuple<Board>();
        for (int possible : possibles) {
            Board b1 = new Board(this);
            MyMove pro = new MyMove(x,y,possible,true);
            MyMove anti = new MyMove(x,y,possible,false);
            pro.applyMove(b1);
            fst.addTuple(b1,anti);
        }

        return fst;
    }


}
