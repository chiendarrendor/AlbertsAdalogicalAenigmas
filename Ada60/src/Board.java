import grid.copycon.CopyCon;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.MultiFlattenSolvable;
import grid.puzzlebits.Direction;
import grid.solverrecipes.singleloopflatten.EdgeState;
import grid.solverrecipes.singleloopflatten.SingleLoopBoard;

import java.util.ArrayList;
import java.util.List;

public class Board extends SingleLoopBoard<Board> implements MultiFlattenSolvable<Board>  {
    @Shallow GridFileReader gfr;
    public Board(String fname) {
        gfr = new GridFileReader(fname);
        init();
    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }


    @Override public int getWidth() { return gfr.getWidth(); }
    @Override public int getHeight() { return gfr.getHeight(); }
    public char getColor(int x,int y) { return gfr.getBlock("COLORS")[x][y].charAt(0); }
    public boolean isDot(int x,int y) { return getColor(x,y) != '.'; }
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public boolean isLetter(int x,int y) { return getLetter(x,y) != '.'; }


    @Override public boolean isComplete() { return getUnknownCount() == 0; }

    public String getSolution() { return gfr.getVar("SOLUTION");  }

    private static class MyMove {
        int x;
        int y;
        Direction d;
        EdgeState es;
        public MyMove(int x,int y,Direction d, EdgeState es) { this.x = x; this.y = y; this.d = d; this.es = es; }

        public boolean applyMove(Board thing) {
            EdgeState cures = thing.getEdge(x,y,d);
            if (cures != EdgeState.UNKNOWN) return cures == es;
            thing.setEdge(x,y,d,es);
            return true;
        }
    }

    @Override public boolean applyMove(Object o) { return ((MyMove)o).applyMove(this); }

    private void getTupleForCellAndDir(List<FlattenSolvableTuple<Board>> list,int x,int y,Direction d) {
        EdgeState cures = getEdge(x,y,d);
        if (cures != EdgeState.UNKNOWN) return;
        Board b1 = new Board(this);
        Board b2 = new Board(this);
        MyMove mm1 = new MyMove(x,y,d,EdgeState.WALL);
        MyMove mm2 = new MyMove(x,y,d,EdgeState.PATH);
        mm1.applyMove(b1);
        mm2.applyMove(b2);
        list.add(new FlattenSolvableTuple<Board>(b1,mm1,b2,mm2));
    }


    @Override public List<FlattenSolvableTuple<Board>> getTuplesForCell(int x, int y) {
        List<FlattenSolvableTuple<Board>> result = new ArrayList<>();
        getTupleForCellAndDir(result,x,y,Direction.EAST);
        getTupleForCellAndDir(result,x,y,Direction.SOUTH);
        return result;
    }

}
