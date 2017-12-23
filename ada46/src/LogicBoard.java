import grid.logic.flatten.FlattenSolvable;
import grid.logic.flatten.FlattenSolvableTuple;

import java.util.List;
import java.util.Vector;

/**
 * Created by chien on 8/12/2017.
 */
public class LogicBoard extends Board implements FlattenSolvable<LogicBoard>
{
    public LogicBoard(String s) { super(s); }
    public LogicBoard(LogicBoard right) { super(right); }

    public boolean isComplete()
    {
        for (int x = 0 ; x < getWidth() ; ++x)
        {
            for (int y = 0 ; y < getHeight() ; ++y)
            {
                if (!hasRegion(x,y)) continue;
                if (!getPossibles(x,y).isSingular()) return false;
            }
        }

        return true;
    }

    // a positive move will remove all but the number in question
    // a negative move will remove the number in question
    private class MyMove
    {
        boolean negative;
        int x;
        int y;
        int num;
        public MyMove(int x,int y,int num,boolean negative)
        {
            this.x = x;
            this.y = y;
            this.num = num;
            this.negative = negative;
        }
    }

    private FlattenSolvableTuple<LogicBoard> getOneTuple(int x,int y)
    {
        if (!hasRegion(x,y)) return null;
        IntegerSet is = getPossibles(x,y);
        if (is.size() < 2) return null;

        FlattenSolvableTuple<LogicBoard> fst = new FlattenSolvableTuple<LogicBoard>();

        for (Integer i : is)
        {
            LogicBoard nb = new LogicBoard(this);
            IntegerSet nbp = nb.getPossibles(x,y);
            nbp.removeAllBut(i);
            MyMove mm = new MyMove(x,y,i,true);
            fst.addTuple(nb,mm);
        }

        return fst;
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


    @Override
    public void applyMove(Object o)
    {
        MyMove mm = (MyMove)o;
        IntegerSet is = getPossibles(mm.x,mm.y);
        if (mm.negative) is.remove(mm.num);
        else is.removeAllBut(mm.num);

    }

    @Override
    public List<LogicBoard> guessAlternatives()
    {
        for (int x = 0 ; x < getWidth() ; ++x)
        {
            for (int y = 0 ; y < getHeight() ; ++y)
            {
                FlattenSolvableTuple<LogicBoard> fst = getOneTuple(x,y);
                if (fst == null) continue;
                Vector<LogicBoard> result = new Vector<>();
                result.addAll(fst.choices);
                return result;
            }
        }
        throw new RuntimeException("Should never reach here!");
    }
}
