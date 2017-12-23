import grid.logic.flatten.FlattenSolvable;
import grid.logic.flatten.FlattenSolvableTuple;

import java.util.List;
import java.util.Vector;


/**
 * Created by chien on 6/7/2017.
 */
public class Board implements FlattenSolvable<Board>
{
    private BoardCore bc;

    BoardCore getBoardCore() { return bc; }

    public Board(BoardCore bc)
    {
        this.bc = bc;
    }

    public Board(Board right)
    {
        this.bc = new BoardCore(right.bc);
    }





    @Override
    public boolean isComplete()
    {
        return bc.getTermCount() == 0;
    }

    private class MyMove
    {
        int ex;
        int ey;
        Edges e;
        public MyMove(int ex,int ey, Edges e) { this.ex = ex ; this.ey = ey ; this.e = e; }
    }

    // operates on an edge coordinate
    private FlattenSolvableTuple<Board> getOneTuple(int x,int y)
    {

        switch(bc.getEdges(x,y))
        {
            // no possible guessing here.
            case ISLAND:
            case LINK:
            case NOTLINK:
            case SLASH:
            case BACKSLASH:
            case NOTANGLE:
                return null;
        }
        Board b1 = new Board(this);
        Board b2 = new Board(this);
        MyMove mm1 = null;
        MyMove mm2 = null;

        switch(bc.getEdges(x,y))
        {
            case UNKNOWNLINK:
                mm1 = new MyMove(x,y,Edges.LINK);
                mm2 = new MyMove(x,y,Edges.NOTLINK);
                break;
            case NOTSLASH:
                mm1 = new MyMove(x,y,Edges.BACKSLASH);
                mm2 = new MyMove(x,y,Edges.NOTANGLE);
                break;
            case NOTBACKSLASH:
                mm1 = new MyMove(x,y,Edges.SLASH);
                mm2 = new MyMove(x,y,Edges.NOTANGLE);
                break;
            case UNKNOWNANGLE:
                mm1 = new MyMove(x,y,Edges.NOTBACKSLASH);
                mm2 = new MyMove(x,y,Edges.NOTSLASH);
                break;
        }
        b1.applyMove(mm1);
        b2.applyMove(mm2);
        return new FlattenSolvableTuple<Board>(b1,mm1,b2,mm2);
    }





    @Override
    public List<FlattenSolvableTuple<Board>> getSuccessorTuples()
    {
        Vector<FlattenSolvableTuple<Board>> result = new Vector<>();
        for (int x = 0 ; x < bc.getEdgeWidth() ; ++x)
        {
            for (int y = 0 ; y < bc.getEdgeHeight() ; ++y)
            {
                FlattenSolvableTuple<Board> fstb = getOneTuple(x,y);
                if (fstb == null) continue;

                result.add(fstb);
            }
        }
        return result;
    }

    @Override
    public void applyMove(Object o)
    {
        MyMove mm = (MyMove)o;
        bc.setEdge(mm.ex,mm.ey,mm.e);
        if (mm.e.isTerminal()) bc.decrementTermCount();
    }

    @Override
    public List<Board> guessAlternatives()
    {
        Vector<Board> result = new Vector<>();

        for (int x = 0 ; x < bc.getEdgeWidth() ; ++x)
        {
            for (int y = 0 ; y < bc.getEdgeHeight() ; ++y)
            {
                FlattenSolvableTuple<Board> fstb = getOneTuple(x,y);
                if (fstb == null) continue;

                result.add(fstb.choices.elementAt(0));
                result.add(fstb.choices.elementAt(1));
                return result;
            }
        }
        throw new RuntimeException("Should never reach here!");
    }
}
