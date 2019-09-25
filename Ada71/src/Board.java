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

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;


public class Board implements StandardFlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Deep CellContainer<CellSet> cells;
    @Shallow int maxnum;
    @Shallow int boxsize;
    @Shallow int boxcount;
    @Shallow List<Rectangle> boxes = new ArrayList<>();

    public Board(String fname) {
        gfr = new GridFileReader(fname);
        maxnum = Integer.parseInt(gfr.getVar("MAXNUM"));
        boxsize = maxnum + 2;
        boxcount = Integer.parseInt(gfr.getVar("NUMBOXES"));
        for (int i = 1 ; i <= boxcount; ++i) {
            String vname = "BOX" + i;
            String var = gfr.getVar(vname);
            String[] parts = var.split(" ");
            Rectangle r = new Rectangle(Integer.parseInt(parts[0]),Integer.parseInt(parts[1]),boxsize,boxsize);
            boxes.add(r);
        }

        cells = new CellContainer<CellSet>(getWidth(),getHeight(),
                (x,y)-> hasLetter(x,y) ? new CellSet(maxnum) : null,
                (x,y,r)-> r != null ? new CellSet(r) : null
        );
    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.'; }
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public CellSet getCell(int x,int y) { return cells.getCell(x,y); }
    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }
    public boolean terminatingForEachCell(BooleanXYLambda bxyl) { return CellLambda.terminatingForEachCell(getWidth(),getHeight(),bxyl); }

    public int getXBlock(int tier,int y) {
        String s = gfr.getBlock("LEFTCLUES")[tier][y];
        if (s.equals(".") || s.equals("?")) return -1;
        return Integer.parseInt(s);
    }

    public int getYBlock(int tier,int x) {
        String s = gfr.getBlock("TOPCLUES")[x][tier];
        if (s.equals(".") || s.equals("?")) return -1;
        return Integer.parseInt(s);
    }



    @Override public boolean isComplete() {
        return terminatingForEachCell((x,y)-> {
            CellSet cs = getCell(x, y);
            if (cs == null) return true;
            return cs.isSolo();
        });
    }

    private static class MyMove {
        int x;
        int y;
        int v;
        boolean isSet;

        public MyMove(int x,int y,int v,boolean isSet) {
            this.x = x;
            this.y = y;
            this.v = v;
            this.isSet = isSet;
        }

        public boolean applyMove(Board b) {
            CellSet cs = b.getCell(x,y);
            if (isSet) {
                if (!cs.has(v)) return false;
                cs.set(v);
                return true;
            } else {
                cs.remove(v);
                return true;
            }
        }
    }


    @Override public boolean applyMove(Object o) { return ((MyMove)o).applyMove(this); }

    @Override public FlattenSolvableTuple<Board> getOneTuple(int x, int y) {
        CellSet cs = getCell(x,y);
        if (cs == null) return null;
        if (cs.count() < 2) return null;

        FlattenSolvableTuple<Board> fst = new FlattenSolvableTuple<>();

        for (int v : cs) {
            Board b = new Board(this);
            MyMove mm = new MyMove(x,y,v,true);
            MyMove anti = new MyMove(x,y,v,false);
            mm.applyMove(b);
            fst.addTuple(b,anti);
        }
        return fst;
    }


}
