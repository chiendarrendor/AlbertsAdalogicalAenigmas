import grid.file.GridFileReader;
import grid.lambda.BooleanXYLambda;
import grid.lambda.CellLambda;
import grid.lambda.XYLambda;

import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.StandardFlattenSolvable;

import java.util.List;

/**
 * Created by chien on 12/10/2017.
 */
public class Board extends StandardFlattenSolvable<Board>
{
    GridFileReader gfr;
    OminoBoard ob;
    int solvesize;

    public Board(String fname)
    {
        gfr = new GridFileReader(fname);

        if (gfr.hasVar("PENTOMINOES"))
        {
            ob = new OminoBoard(this,5,gfr.getVar("PENTOMINOES"));
            solvesize = gfr.getVar("PENTOMINOES").length();
        }
        else if (gfr.hasVar("TETRONIMOES"))
        {
            ob = new OminoBoard(this,4,gfr.getVar("TETRONIMOES"));
            solvesize = gfr.getVar("TETROMINOES").length();
        }
        else throw new RuntimeException("Unknown or missing OMINO specifier variable");

    }

    public Board(Board right)
    {
        gfr = right.gfr;
        ob = new OminoBoard(this,right.ob);
        solvesize = right.solvesize;
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }

    @Override
    public boolean isComplete()
    {
        return ob.solvedPlaceSize() == solvesize;
    }

    private class MyMove
    {
        int x;
        int y;
        CellColor cc;
        public MyMove(int x,int y,CellColor cc ) { this.x = x; this.y = y; this.cc = cc; }
    }


    @Override
    public void applyMove(Object o)
    {
        MyMove mm = (MyMove) o;
        ob.cells[mm.x][mm.y].requiredColor = mm.cc;
    }

    @Override
    public FlattenSolvableTuple<Board> getOneTuple(int x, int y)
    {
        if (ob.cells[x][y].requiredColor != CellColor.UNKNOWN) return null;
        Board b1 = new Board(this);
        MyMove mm1 = new MyMove(x,y,CellColor.BLACK);
        b1.applyMove(mm1);

        Board b2 = new Board(this);
        MyMove mm2 = new MyMove(x,y,CellColor.WHITE);
        b2.applyMove(mm2);

        return new FlattenSolvableTuple<Board>(b1,mm1,b2,mm2);
    }

    public int getNumber(int x,int y)
    {
        String s = gfr.getBlock("NUMBERS")[x][y];
        if (s.equals(".")) return -1;
        return Integer.parseInt(s);
    }
    public CellColor getOrigCellColor(int x, int y)
    {
        switch(gfr.getBlock("DOTS")[x][y].charAt(0))
        {
            case 'B': return CellColor.BLACK;
            case 'W': return CellColor.WHITE;
            default: return CellColor.UNKNOWN;
        }
    }


    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl);}
    public boolean terminatingForEachCell(BooleanXYLambda bxyl)
    {
        return CellLambda.terminatingForEachCell(getWidth(),getHeight(),bxyl);
    }

    public boolean onBoard(int x,int y) { return gfr.inBounds(x,y); }







}
