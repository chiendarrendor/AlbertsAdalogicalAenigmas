import grid.copycon.CopyCon;
import grid.copycon.Ignore;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.MultiFlattenSolvable;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;
import grid.solverrecipes.singleloopflatten.EdgeState;
import grid.solverrecipes.singleloopflatten.SingleLoopBoard;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Board extends SingleLoopBoard<Board> implements MultiFlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Shallow CellContainer<Integer> terminals;
    @Shallow List<Point> lovers = new ArrayList<>();
    @Ignore int depth;

    public Board(String fname) {
        depth = 1;
        gfr = new GridFileReader(fname);
        terminals = new CellContainer<Integer>(getWidth(),getHeight(),
            (x,y)-> {
                String e = gfr.getBlock("ENDS")[x][y];
                if (e.equals(".")) return -1;
                if (e.equals("H")) return 0;
                if (e.matches("\\d+")) {
                    lovers.add(new Point(x,y));
                    return Integer.parseInt(e);
                }
                throw new RuntimeException("Illegal ENDS entry " + e);
            });


        init();
    }

    public Board(Board right) {
        depth = right.depth + 1;
        CopyCon.copy(this,right);
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }

    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.'; }

    public boolean isSpace(int x,int y) { return terminals.getCell(x,y) == -1; }
    public boolean isTerminal(int x, int y) { return !isSpace(x,y); }
    public boolean isMate(int x, int y) { return terminals.getCell(x,y) == 0; }
    public boolean isLover(int x,int y) { return terminals.getCell(x,y) > 0; }
    public int getLoverNumber(int x,int y) { return terminals.getCell(x,y); }

    @Override public boolean isComplete() { return getUnknownCount() == 0; }

    public static class MyMove {
        int x;
        int y;
        Direction d;
        EdgeState es;
        public MyMove(int x,int y,Direction d, EdgeState es) { this.x = x; this.y = y; this.d = d; this.es = es; }
        public boolean applyMove(Board thing) {
            if (thing.getEdge(x,y,d) != EdgeState.UNKNOWN) return thing.getEdge(x,y,d) == es;
            thing.setEdge(x,y,d,es);
            return true;
        }
    }

    @Override public boolean applyMove(Object o) { return ((MyMove)o).applyMove(this); }

    public List<FlattenSolvableTuple<Board>> getTuplesForCell(int x, int y) {
        List<FlattenSolvableTuple<Board>> result = new ArrayList<>();
        for (Direction d: Direction.orthogonals()) {
            if (getEdge(x,y,d) != EdgeState.UNKNOWN) continue;
            Board b1 = new Board(this);
            Board b2 = new Board(this);
            MyMove mm1 = new MyMove(x,y,d,EdgeState.WALL);
            MyMove mm2 = new MyMove(x,y,d,EdgeState.PATH);
            mm1.applyMove(b1);
            mm2.applyMove(b2);
            result.add(new FlattenSolvableTuple<Board>(b1,mm1,b2,mm2));
        }
        return result;
    }




}
