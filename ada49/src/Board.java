import grid.file.GridFileReader;
import grid.lambda.BooleanXYLambda;
import grid.lambda.CellLambda;
import grid.lambda.XYLambda;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.StandardFlattenSolvable;

/**
 * Created by chien on 11/6/2017.
 */
public class Board extends StandardFlattenSolvable<Board>
{
    GridFileReader gfr;
    CellState cells[][];
    int numunknown;
    Clues clues;
    ClueUnrolled unrolls[][];

    public Board(String fname)
    {
        gfr = new GridFileReader(fname);
        cells = new CellState[getWidth()][getHeight()];
        unrolls = new ClueUnrolled[getWidth()][getHeight()];
        clues = new Clues(this);
        forEachCell((x,y)-> {
            cells[x][y] = CellState.UNKNOWN;
            if (clues.clues[x][y] != null)
            {
                unrolls[x][y] = new ClueUnrolled(clues.clues[x][y]);
            }
        } );
        numunknown = getWidth() * getHeight();

    }

    public Board(Board right)
    {
        gfr = right.gfr;
        cells = new CellState[getWidth()][getHeight()];
        unrolls = new ClueUnrolled[getWidth()][getHeight()];

        forEachCell((x,y)-> {
            cells[x][y] = right.getCell(x,y);
            if (right.unrolls[x][y] != null) unrolls[x][y] = new ClueUnrolled(right.unrolls[x][y]);
        } );
        numunknown = right.numunknown;
        clues = right.clues;
    }

    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }

    public CellState getCell(int x, int y) { return cells[x][y]; }
    public void setCell(int x,int y,CellState cs) { cells[x][y] = cs; --numunknown; }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public boolean onBoard(int x,int y) { return gfr.inBounds(x,y); }

    @Override
    public boolean isComplete()
    {
        return numunknown == 0;
    }

    private class MyMove
    {
        int x;
        int y;
        CellState cs;
        public MyMove(int ix,int iy,CellState ics) { x = ix; y = iy; cs = ics;}
    }

    @Override
    public void applyMove(Object o)
    {
        MyMove mm = (MyMove)o;
        setCell(mm.x,mm.y,mm.cs);
    }

    @Override
    public FlattenSolvableTuple<Board> getOneTuple(int x, int y)
    {
        if (getCell(x,y) != CellState.UNKNOWN) return null;
        Board b1 = new Board(this);
        b1.setCell(x,y,CellState.WHITE);
        MyMove mm1 = new MyMove(x,y,CellState.WHITE);

        Board b2 = new Board(this);
        b2.setCell(x,y,CellState.BLACK);
        MyMove mm2 = new MyMove(x,y,CellState.BLACK);

        return new FlattenSolvableTuple<Board>(b1,mm1,b2,mm2);
    }

    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }
    public boolean terminatingForEachCell(BooleanXYLambda bxyl)
    {
        return CellLambda.terminatingForEachCell(getWidth(),getHeight(),bxyl);
    }



}
