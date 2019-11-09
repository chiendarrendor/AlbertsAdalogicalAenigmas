import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;
import grid.puzzlebits.EdgeContainer;
import grid.puzzlebits.Path.GridPathContainer;
import grid.puzzlebits.Path.Path;
import grid.solverrecipes.singleloopflatten.EdgeState;
import grid.solverrecipes.singleloopflatten.SingleLoopBoard;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Board extends SingleLoopBoard<Board>  {
    @Shallow GridFileReader gfr;
    @Deep CellContainer<CellType> cells;
    @Shallow int cellunknowns;
    @Shallow List<Point> numberclues = new ArrayList<>();
    @Shallow CellContainer<Character> finalclues;

    public Board(String fname) {
        gfr = new GridFileReader(fname);
        init();

        finalclues = new CellContainer<Character>(getWidth(),getHeight(),(x,y)->' ');

        cells = new CellContainer<CellType>(getWidth(),getHeight(),(x,y)-> {
            if (isNumberClue(x,y)) numberclues.add(new Point(x,y));
            return CellType.UNKNOWN;
        });
        cellunknowns = getWidth() * getHeight();

        numberclues.sort((a,b)->Integer.compare(getNumberClue(b.x,b.y),getNumberClue(a.x,a.y)) );
    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }


    public void setLetter(int x,int y, char c) { finalclues.setCell(x,y,c); }
    public char getFinalLetter(int x,int y) { return finalclues.getCell(x,y); }
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0);  }
    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.'; }

    public boolean isNumberClue(int x,int y) {  return "0123456789".indexOf(gfr.getBlock("CLUES")[x][y].charAt(0)) != -1; }
    public int getNumberClue(int x,int y) { return Integer.parseInt(gfr.getBlock("CLUES")[x][y]); }
    public boolean isArrowClue(int x,int y) {  return "<>v^".indexOf(gfr.getBlock("CLUES")[x][y].charAt(0)) != -1; }
    public char getArrowClue(int x,int y) { return gfr.getBlock("CLUES")[x][y].charAt(0); }



    public GridPathContainer getPathContainer() { return paths; }

    // only valid if this is a solution...
    public Path getPathAt(int x,int y) {
        if (getCellType(x,y) == CellType.TERMINAL) {
            return paths.getCell(x,y).getTerminalPaths().get(0);
        } else {
            return paths.getCell(x, y).getInternalPaths().get(0);
        }
    }
    public boolean pathHasClue(Path p) {
        for (Point pt : p) {
            if (isNumberClue(pt.x,pt.y) || isArrowClue(pt.x,pt.y)) return true;
        }
        return false;
    }




    @Override public int getWidth() {  return gfr.getWidth();  }
    @Override public int getHeight() { return gfr.getHeight(); }

    public CellType getCellType(int x,int y) { return cells.getCell(x,y); }
    public void setCellType(int x,int y,CellType ct) {
        if (ct.isFinal()) --cellunknowns;
        cells.setCell(x,y,ct);
    }




    @Override public boolean isComplete() { return getUnknownCount() == 0 && cellunknowns == 0; }

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
        CellGuessOrder cgo = new CellGuessOrder(this);
        Set<EdgeContainer.EdgeCoord> seenedges = new HashSet<>();

        for (Point p : cgo) {
            for (Direction d: Direction.orthogonals()) {
                EdgeState es = getEdge(p.x,p.y,d);
                if (es != EdgeState.UNKNOWN) continue;
                EdgeContainer.EdgeCoord ec = EdgeContainer.getEdgeCoord(p.x,p.y,d);
                if (seenedges.contains(ec)) continue;
                seenedges.add(ec);

                MyMove mm1 = new MyMove(ec.x,ec.y,ec.isV,EdgeState.PATH);
                MyMove mm2 = new MyMove(ec.x,ec.y,ec.isV,EdgeState.WALL);
                Board b1 = new Board(this);
                Board b2 = new Board(this);
                mm1.applyMove(b1);
                mm2.applyMove(b2);
                result.add(new FlattenSolvableTuple<Board>(b1,mm1,b2,mm2));

                if (getOnlyOne) return result;
            }
        }

        return result;
    }

    @Override public boolean applyMove(Object o) { return ((MyMove)o).applyMove(this); }
    @Override public List<FlattenSolvableTuple<Board>> getSuccessorTuples() { return getTuples(false); }
    @Override public List<Board> guessAlternatives() { return getTuples(true).get(0).choices; }
}
