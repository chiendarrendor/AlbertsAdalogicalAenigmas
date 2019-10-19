import grid.copycon.CopyCon;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.puzzlebits.EdgeContainer;
import grid.puzzlebits.Path.Path;
import grid.solverrecipes.singleloopflatten.EdgeState;
import grid.solverrecipes.singleloopflatten.SingleLoopBoard;

import java.util.ArrayList;
import java.util.List;


public class Board extends SingleLoopBoard<Board>  {
    @Shallow GridFileReader gfr;
    public Board(String fname) {
        gfr = new GridFileReader(fname);
        init();
    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }


    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0);  }
    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.'; }
    public boolean hasClue(int x,int y) { return gfr.getBlock("CLUES")[x][y].charAt(0) != '.'; }
    public int getClue(int x,int y) { return Integer.parseInt(gfr.getBlock("CLUES")[x][y]); }
    // only valid if this is a solution...
    public Path getPathAt(int x,int y) { return paths.getCell(x,y).getInternalPaths().get(0);}

    public List<PathInfo> getPathInfo() {
        List<PathInfo> result = new ArrayList<>();
        getPaths().forEach(p->result.add(new PathInfo(this,p)));
        return result;
    }

    @Override public int getWidth() {  return gfr.getWidth();  }
    @Override public int getHeight() { return gfr.getHeight(); }
    @Override public boolean isComplete() { return getUnknownCount() == 0; }

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

    private List<FlattenSolvableTuple<Board>> getTuples(boolean getOnlyOne) {
        List<FlattenSolvableTuple<Board>> result = new ArrayList<>();
        EdgeOrdering eo = new EdgeOrdering(this);

        for(EdgeContainer.EdgeCoord eoc : eo) {
            MyMove mm1 = new MyMove(eoc.x,eoc.y,eoc.isV,EdgeState.PATH);
            MyMove mm2 = new MyMove(eoc.x,eoc.y,eoc.isV,EdgeState.WALL);
            Board b1 = new Board(this);
            Board b2 = new Board(this);
            mm1.applyMove(b1);
            mm2.applyMove(b2);
            result.add(new FlattenSolvableTuple<Board>(b1,mm1,b2,mm2));
            if (getOnlyOne) break;
        }

        return result;
    }

    @Override public boolean applyMove(Object o) { return ((MyMove)o).applyMove(this); }
    @Override public List<FlattenSolvableTuple<Board>> getSuccessorTuples() { return getTuples(false); }
    @Override public List<Board> guessAlternatives() { return getTuples(true).get(0).choices; }
}
