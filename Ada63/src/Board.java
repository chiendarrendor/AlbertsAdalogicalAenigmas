import grid.copycon.CopyCon;
import grid.copycon.Ignore;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.file.SubReader;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.MultiFlattenSolvable;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Board implements MultiFlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Shallow int width;
    @Shallow int height;
    @Ignore SubBoard[] subboards;


    public Board(String fname) {
        gfr = new GridFileReader(fname);
        int boardcount = Integer.parseInt(gfr.getVar("BOARDCOUNT"));
        if (boardcount < 1) throw new RuntimeException("BOARDCOUNT must be a positive number!");
        height = gfr.getHeight();
        if (gfr.getWidth() % boardcount != 0) throw new RuntimeException("Specified Grid Width must be divisible by BOARDCOUNT");
        width = gfr.getWidth() / boardcount;

        subboards = new SubBoard[boardcount];
        for (int i = 0 ; i < boardcount ; ++i) {
            subboards[i] = new SubBoard(i,new SubReader(gfr,width*i,0,width,height));
        }

    }

    public Board(Board right) {
        CopyCon.copy(this,right);
        subboards = new SubBoard[right.subboards.length];
        for (int i = 0 ; i < getBoardCount() ; ++i) subboards[i] = new SubBoard(right.subboards[i]);
    }


    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean inBounds(int x,int y) { return x >= 0 && y >= 0 && x < width && y < height; }
    public boolean inBounds(Point p) { return inBounds(p.x,p.y); }


    public int getBoardCount() { return subboards.length; }
    public int getUnknowns() { return Arrays.stream(subboards).mapToInt(SubBoard::getUnknowns).sum();  }
    public SubBoard getSubBoard(int id) { return subboards[id]; }

    @Override public boolean isComplete() {
        return getUnknowns() == 0;
    }

    private static class MyMove {
        int bid;
        int x;
        int y;
        CellState cs;
        public MyMove(int bid,int x,int y,CellState cs) { this.bid = bid; this.x = x; this.y = y; this.cs = cs; }
        public boolean applyMove(Board b) {
            SubBoard sb = b.getSubBoard(bid);
            CellState ocs = sb.getCell(x,y);
            if (ocs != CellState.UNKNOWN) return ocs == cs;
            sb.setCell(x,y,cs);
            return true;
        }
    }


    @Override public boolean applyMove(Object o) {
        return ((MyMove)o).applyMove(this);
    }

    @Override public List<FlattenSolvableTuple<Board>> getTuplesForCell(int x, int y) {
        List<FlattenSolvableTuple<Board>> result = new ArrayList<>();
        for (int i = 0 ; i < getBoardCount() ; ++i) {
            SubBoard sb = this.getSubBoard(i);
            if (sb.getCell(x,y) != CellState.UNKNOWN) continue;
            Board b1 = new Board(this);
            Board b2 = new Board(this);
            MyMove mm1 = new MyMove(i,x,y,CellState.WHITE);
            MyMove mm2 = new MyMove(i,x,y,CellState.BLACK);
            mm1.applyMove(b1);
            mm2.applyMove(b2);
            result.add(new FlattenSolvableTuple<Board>(b1,mm1,b2,mm2));
        }

        return result;
    }



}
