import grid.logic.flatten.FlattenSolvable;
import grid.logic.flatten.FlattenSolvableTuple;

import java.util.List;
import java.util.Vector;

/**
 * Created by chien on 8/17/2017.
 */
public class LogicBoard extends Board implements FlattenSolvable<LogicBoard>
{
    public LogicBoard(String fname) { super(fname);}
    public LogicBoard(LogicBoard right) { super(right); }
    public boolean isComplete() { return unknowncount == 0; }

    private FlattenSolvableTuple<LogicBoard> getOneTuple(int x,int y)
    {
        if (getCell(x,y) != CellState.UNKWNOWN) return null;

        LogicBoard lb1 = new LogicBoard(this);
        lb1.setCellLand(x,y);
        MyMove mm1 = new MyMove(x,y,CellState.LAND);

        LogicBoard lb2 = new LogicBoard(this);
        lb2.setCellRiver(x,y);
        MyMove mm2 = new MyMove(x,y,CellState.RIVER);

        return new FlattenSolvableTuple<>(lb1, mm1, lb2, mm2);
    }




    @Override
    public List<FlattenSolvableTuple<LogicBoard>> getSuccessorTuples()
    {
        Vector<FlattenSolvableTuple<LogicBoard>> result = new Vector<>();
        for (int x = 0 ; x < getWidth() ; ++x)
        {
            for (int y = 0 ; y < getHeight() ; ++y)
            {
                FlattenSolvableTuple<LogicBoard> fst = getOneTuple(x,y);
                if (fst == null) continue;
                result.add(fst);
            }
        }
        return result;
    }

    private class MyMove
    {
        int x;
        int y;
        CellState cs;
        public MyMove(int x,int y, CellState cs) { this.x = x ; this.y = y; this.cs = cs; }
    }

    @Override
    public void applyMove(Object o)
    {
        MyMove mm = (MyMove)o;
        if (mm.cs == CellState.LAND) setCellLand(mm.x,mm.y);
        else if (mm.cs == CellState.RIVER) setCellRiver(mm.x,mm.y);
        else throw new RuntimeException("Invalid Cell State in apply move");
    }

    @Override
    public List<LogicBoard> guessAlternatives()
    {
        for (int x = 0; x < getWidth(); ++x)
        {
            for (int y = 0; y < getHeight(); ++y)
            {
                FlattenSolvableTuple<LogicBoard> fst = getOneTuple(x, y);
                if (fst == null) continue;
                return fst.choices;
            }
        }
        throw new RuntimeException("should not guess on a solved board!");
    }
}
