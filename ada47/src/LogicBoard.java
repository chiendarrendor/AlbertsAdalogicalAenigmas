import grid.logic.flatten.FlattenSolvable;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.puzzlebits.Direction;
import sun.rmi.runtime.Log;

import java.awt.Point;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Vector;

/**
 * Created by chien on 9/3/2017.
 */
public class LogicBoard extends Board implements FlattenSolvable<LogicBoard>
{
    public LogicBoard(String fname) { super(fname); }
    public LogicBoard(LogicBoard right) { super(right); }


    @Override
    public boolean isComplete()
    {
        return unknowns == 0;
    }

    private FlattenSolvableTuple<LogicBoard> getOneTuple(int x,int y,Direction d)
    {
           if (this.getEdge(x,y,d) != EdgeType.UNKNOWN) return null;

           LogicBoard wb = new LogicBoard(this);
           wb.setEdge(x,y,d,EdgeType.NOTPATH);
           MyMove mmw = new MyMove(x,y,d,EdgeType.NOTPATH);

           LogicBoard pb = new LogicBoard(this);
           pb.setEdge(x,y,d,EdgeType.PATH);
           MyMove mmp = new MyMove(x,y,d,EdgeType.PATH);

           return new FlattenSolvableTuple<LogicBoard>(wb,mmw,pb,mmp);
    }





    @Override
    public List<FlattenSolvableTuple<LogicBoard>> getSuccessorTuples()
    {
        Vector<FlattenSolvableTuple<LogicBoard>> result = new Vector<>();

        forEachCell( (x,y) -> {
            for(Direction d : Direction.orthogonals())
            {
                FlattenSolvableTuple<LogicBoard> fst = getOneTuple(x,y,d);
                if (fst == null) continue;
                result.add(fst);
            }
            return true;
        });
        return result;
    }

    private class MyMove
    {
        int x;
        int y;
        Direction d;
        EdgeType et;
        public MyMove(int x,int y,Direction d,EdgeType et) {this.x = x ; this.y = y; this.d = d; this.et = et; }
        public boolean Apply(Board b)
        {
            if (b.getEdge(x,y,d) != EdgeType.UNKNOWN) return b.getEdge(x,y,d) == et;
            b.setEdge(x,y,d,et);
            return true;
        }
    }

    @Override
    public boolean applyMove(Object o)
    {
        return ((MyMove)o).Apply(this);
    }

    @Override
    public List<LogicBoard> guessAlternatives()
    {
        Vector<FlattenSolvableTuple<LogicBoard>> result = new Vector<>();

        forEachCell( (x,y) -> {
            for(Direction d : Direction.orthogonals())
            {
                FlattenSolvableTuple<LogicBoard> fst = getOneTuple(x,y,d);
                if (fst == null) continue;
                result.add(fst);
                return false;
            }
            return true;
        });

        if (result.size() == 0) throw new RuntimeException("guessAlternatives should only be called on a non-terminal board!");
        return result.elementAt(0).choices;
    }
}
