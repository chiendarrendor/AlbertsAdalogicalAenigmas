import grid.assistant.AssistantBoard;
import grid.assistant.AssistantMove;
import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.lambda.CellLambda;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.StandardFlattenSolvable;
import grid.puzzlebits.CellContainer;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class Board implements StandardFlattenSolvable<Board>,AssistantBoard<Board> {
    @Shallow GridFileReader gfr;
    @Deep CellContainer<Cell> cells;
    @Shallow  int maxcount;

    public Board(String fname) {
        gfr = new GridFileReader(fname);
        maxcount = Integer.parseInt(gfr.getVar("MAXNUM"));

        cells = new CellContainer<Cell>(getWidth(),getHeight(),
                (x,y)-> inBounds(x,y) ? new Cell(maxcount) : null,
                (x,y,r)-> inBounds(x,y) ? new Cell(r) : null
        );


    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.'; }
    public boolean inBounds(int x,int y) { return gfr.inBounds(x,y) && hasLetter(x,y); }
    public String getHorizontalClue(int x,int y) { return gfr.getBlock("HORIZONTALS")[x][y]; }
    public String getVerticalClue(int x,int y) { return gfr.getBlock("VERTICALS")[x][y]; }
    public String getSolution() { return gfr.getVar("SOLUTION"); }
    public Cell getCell(int x,int y) { return cells.getCell(x,y); }
    public int getMaxCount() { return maxcount; }
    public List<Rectangle> getBoxes() {
        List<Rectangle> result = new ArrayList<>();
        int boxcount = Integer.parseInt(gfr.getVar("BOXCOUNT"));
        int boxsize = Integer.parseInt(gfr.getVar("BOXSIZE"));

        for (int i = 1 ; i <= boxcount ; ++i) {
            String boxid = "BOX" + i;
            String[] boxul = gfr.getVar(boxid).split(" ");
            result.add(new Rectangle(Integer.parseInt(boxul[0]),Integer.parseInt(boxul[1]),boxsize,boxsize));
        }
        return result;
    }


    @Override public boolean isComplete() {
        return CellLambda.stream(getWidth(),getHeight()).allMatch((p)->{
            Cell c = getCell(p.x,p.y);
            if (c == null) return true;
            return c.isComplete();
        });
    }

    @Override public Board clone() {
        return new Board(this);
    }

    public static class MyMove implements AssistantMove<Board> {
        private int x;
        private int y;
        private boolean isOnly;
        private int item;
        public MyMove(int x,int y,boolean isOnly, int item) {
            this.x = x;
            this.y = y;
            this.isOnly = isOnly;
            this.item = item;
        }

        public boolean applyMove(Board thing) {
            Cell c = thing.getCell(x,y);
            if (isOnly) {
                if (!c.contains(item)) return false;
                c.removeAllBut(item);
            }
            else {
                c.remove(item);
            }
            return true;
        }

        public int getX() { return x; }
        public int getY() { return y; }
        public boolean isOnly() { return isOnly; }
        public int getItem() { return item; }

    }


    @Override public boolean applyMove(Object o) {
        MyMove mm = (MyMove)o;
        return mm.applyMove(this);
    }

    @Override public FlattenSolvableTuple<Board> getOneTuple(int x, int y) {
        Cell c = this.getCell(x,y);
        if (c == null || c.isComplete()) return null;
        FlattenSolvableTuple<Board> result = new FlattenSolvableTuple<Board>();

        c.stream().forEach((item) -> {
                Board nb = new Board(this);
                Cell nc = nb.getCell(x,y);
                MyMove tm = new MyMove(x,y,true,item);
                MyMove antimove = new MyMove(x,y,false,item);
                tm.applyMove(nb);
                result.addTuple(nb,antimove);
            }
        );

        return result;
    }


    // disable the Flatten stage, I don't think it's useful given BetterClueLogicStep
    @Override public List<FlattenSolvableTuple<Board>> getSuccessorTuples() {
        List<FlattenSolvableTuple<Board>> result = new ArrayList<>();

        return result;
    }


}
