import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.lambda.BooleanXYLambda;
import grid.lambda.CellLambda;
import grid.lambda.LambdaInteger;
import grid.lambda.XYLambda;
import grid.logic.flatten.FlattenSolvable;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.puzzlebits.CellContainer;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Board implements FlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Shallow int unknowns;
    @Deep CellContainer<CellState> cells;



    public Board(String fname) {
        gfr = new GridFileReader(fname);

        LambdaInteger lunk = new LambdaInteger(0);
        cells = new CellContainer<CellState>(getWidth(),getHeight(),(x,y)->{
            if (hasClue(x,y)) {
                return CellState.PATH;
            } else {
                lunk.inc();
                return CellState.UNKNOWN;
            }
        });
        unknowns = lunk.get();


    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.'; }
    public boolean hasClue(int x,int y) { return gfr.getBlock("CLUES")[x][y].charAt(0) != '.'; }
    public int getClue(int x,int y) { return Integer.parseInt(gfr.getBlock("CLUES")[x][y]); }
    public boolean inBounds(int x, int y) { return gfr.inBounds(x,y); }
    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }
    public boolean terminatingForEachCell(BooleanXYLambda bxyl) { return CellLambda.terminatingForEachCell(getWidth(),getHeight(),bxyl); }

    public CellState getCellState(int x,int y) { return cells.getCell(x,y); }
    public void setCellState(int x,int y,CellState cs) { cells.setCell(x,y,cs);}

    private static class MyMove {
        int x;
        int y;
        CellState cs;
        public MyMove(int x,int y,CellState cs) { this.x = x; this.y = y; this.cs = cs; }
        public boolean applyMove(Board b) {
            CellState rcs = b.getCellState(x,y);
            if (rcs != CellState.UNKNOWN) return rcs == cs;
            b.setCellState(x,y,cs);

            return true;
        }
    }

    List<FlattenSolvableTuple<Board>> getTuples(boolean onlyOne) {
        List<FlattenSolvableTuple<Board>> result = new ArrayList<>();
        terminatingForEachCell((x,y)-> {
           if (getCellState(x,y) != CellState.UNKNOWN) return true;
            Board b1 = new Board(this);
            Board b2 = new Board(this);
            MyMove mm1 = new MyMove(x,y,CellState.PATH);
            MyMove mm2 = new MyMove(x,y,CellState.WALL);
            mm1.applyMove(b1);
            mm2.applyMove(b2);
            result.add(new FlattenSolvableTuple<Board>(b1,mm1,b2,mm2));
            return !onlyOne;
        });
        return result;
    }


    @Override public boolean isComplete() { return unknowns == 0; }
    @Override public boolean applyMove(Object o) { return ((MyMove)o).applyMove(this); }
    @Override public List<FlattenSolvableTuple<Board>> getSuccessorTuples() { return getTuples(false); }
    @Override public List<Board> guessAlternatives() { return getTuples(true).get(0).choices; }
}
