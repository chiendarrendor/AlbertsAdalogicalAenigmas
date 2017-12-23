import grid.logic.flatten.FlattenSolvable;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.puzzlebits.Direction;

import java.util.List;
import java.util.Vector;

/**
 * Created by chien on 7/23/2017.
 */
public class LogicBoard implements FlattenSolvable<LogicBoard>
{
    private Board b;

    public LogicBoard(Board b)
    {
        this.b = b;
    }

    public LogicBoard(LogicBoard right)
    {
        b = new Board(right.b);
    }

    public Board getBoard() { return b; }


    @Override
    public boolean isComplete()
    {
        for (int x = 0 ; x < b.getWidth() ; ++x)
        {
            for (int y = 0 ; y < b.getHeight() ; ++y)
            {
                for (Direction dir : Direction.orthogonals()) { if (b.getEdge(x,y,dir) == EdgeState.UNKNOWN) return false; }
            }
        }
        return true;
    }

    private class MyMove
    {
        int x;
        int y;
        Direction d;
        EdgeState es;
        public MyMove(int x,int y,Direction d, EdgeState es) { this.x = x; this.y = y; this.d = d; this.es = es; }
        public String toString() { return "X: " + x + " Y " + y + " D: " + d + " state: " + es; }
    }

    private FlattenSolvableTuple<LogicBoard> getOneTuple(int x,int y,Direction d)
    {
        LogicBoard lb1 = new LogicBoard(this);
        LogicBoard lb2 = new LogicBoard( this );
        lb1.getBoard().wall(x,y,d);
        MyMove mm1 = new MyMove(x,y,d,EdgeState.WALL);
        lb2.getBoard().path(x,y,d);
        MyMove mm2 = new MyMove(x,y,d,EdgeState.PATH);

        return new FlattenSolvableTuple<LogicBoard>(lb1,mm1,lb2,mm2);

    }

    @Override
    public List<FlattenSolvableTuple<LogicBoard>> getSuccessorTuples()
    {
//        System.out.println("He's flailing!");
        Vector<FlattenSolvableTuple<LogicBoard>> result = new Vector<>();
        for (int x = 0; x < b.getWidth(); ++x)
        {
            for (int y = 0; y < b.getHeight(); ++y)
            {
                for (Direction dir : Direction.orthogonals())
                {
                    if (b.getEdge(x, y, dir) != EdgeState.UNKNOWN) continue;
                    result.add(getOneTuple(x, y, dir));
                }
            }
        }
        return result;
    }

    @Override
    public void applyMove(Object o)
    {
        MyMove mm = (MyMove)o;

        if (mm.es == EdgeState.PATH) b.path(mm.x,mm.y,mm.d);
        else b.wall(mm.x,mm.y,mm.d);

    }

    @Override
    public List<LogicBoard> guessAlternatives()
    {
        for (int x = 0; x < b.getWidth(); ++x)
        {
            for (int y = 0; y < b.getHeight(); ++y)
            {
                for (Direction dir : Direction.orthogonals())
                {
                    if (b.getEdge(x, y, dir) != EdgeState.UNKNOWN) continue;
                    FlattenSolvableTuple<LogicBoard> fst = getOneTuple(x,y,dir);
                    return fst.choices;
                }
            }
        }
        throw new RuntimeException("Should never get here!");
    }
}
