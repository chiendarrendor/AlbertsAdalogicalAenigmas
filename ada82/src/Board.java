import grid.copycon.CopyCon;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.logic.flatten.FlattenSolvable;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.puzzlebits.Direction;
import grid.puzzlebits.EdgeContainer;
import grid.solverrecipes.singleloopflatten.EdgeState;
import grid.solverrecipes.singleloopflatten.SingleLoopBoard;

import java.util.ArrayList;
import java.util.List;


public class Board extends SingleLoopBoard<Board> implements FlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Shallow EdgeContainer<Boolean> edgeClues;

    public Board(String fname) {
        gfr = new GridFileReader(fname);
        init();

        edgeClues = new EdgeContainer<Boolean>(getWidth(),getHeight(),Boolean.FALSE,
                (x,y,isV)-> false,
                (x,y,isV,old)->old);

        forEachCell((x,y)-> {
            if (hasRawClue(x,y)) {
                if (getRawClue(x,y) == '@') return;
                edgeClues.setEdge(x,y, Direction.fromShort(""+getRawClue(x,y)),true);
            }
        });


    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.'; }
    public char getRawClue(int x,int y) { return gfr.getBlock("CLUES")[x][y].charAt(0); }
    public boolean hasRawClue(int x,int y) { return getRawClue(x,y) != '.'; }
    public boolean hasCenterClue(int x,int y) { return getRawClue(x,y) == '@'; }
    public boolean hasEdgeClue(int x,int y,Direction d) { return edgeClues.getEdge(x,y,d); }

    public boolean hasClue(int x,int y) {
        if (getRawClue(x,y) == '@') return true;
        for (Direction d : Direction.orthogonals()) {
            if (edgeClues.getEdge(x,y,d) == true) return true;
        }
        return false;
    }

    private static class MyMove {
        int x;
        int y;
        boolean isV;
        EdgeState es;

        public MyMove(int x,int y,boolean isV,EdgeState es) { this.x = x; this.y = y; this.isV = isV; this.es = es; }
        public boolean applyMove(Board b) {
            if (b.getEdge(x,y,isV) != EdgeState.UNKNOWN) return b.getEdge(x,y,isV) == es;
            b.setEdge(x,y,isV,es);
            return true;
        }
    }

    @Override public boolean isComplete() { return this.getUnknownCount() == 0; }
    @Override public boolean applyMove(Object o) { return ((MyMove)o).applyMove(this); }

    private FlattenSolvableTuple<Board> getTupleForEdge(int x,int y,boolean isV) {
        if (getEdge(x,y,isV) != EdgeState.UNKNOWN) return null;
        Board b1 = new Board(this);
        Board b2 = new Board(this);
        MyMove mm1 = new MyMove(x,y,isV,EdgeState.PATH);
        MyMove mm2 = new MyMove(x,y,isV,EdgeState.WALL);
        mm1.applyMove(b1);
        mm2.applyMove(b2);
        return new FlattenSolvableTuple<Board>(b1,mm1,b2,mm2);
    }

    @Override public List<FlattenSolvableTuple<Board>> getSuccessorTuples() {
        List<FlattenSolvableTuple<Board>> result = new ArrayList<>();
        forEachEdge((x,y,isV,old)->{
            FlattenSolvableTuple<Board> fst = getTupleForEdge(x,y,isV);
            if (fst == null) return;
            result.add(fst);
        });
        return result;
    }

    @Override public List<Board> guessAlternatives() {
        List<FlattenSolvableTuple<Board>> set = getSuccessorTuples();
        return set.get(0).choices;
    }


}
