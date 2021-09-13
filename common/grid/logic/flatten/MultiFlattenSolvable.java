package grid.logic.flatten;

import grid.logic.ContainerRuntimeException;

import java.awt.Container;
import java.util.List;
import java.util.Vector;

/**
 * Created by chien on 10/1/2017.
 *  This class implements a very standard behavior for this type of puzzle
 *  where there one or more tuples can be created for each cell of a grid for successor tuples
 *  and a valid guess is the boards of the first non-null tuple we come across.
 *
 */
public interface MultiFlattenSolvable<T> extends FlattenSolvable<T>
{
    public int getWidth();
    public int getHeight();

    // required for FlattenSolvable
    public boolean isComplete();
    // applies a move object returned in a tuple returned by getOneTuple()
    // (required for FlattenSolvable)
    public boolean applyMove(Object o);

    // returns null if this particular cell is already solved.
    public List<FlattenSolvableTuple<T>> getTuplesForCell(int x,int y);





    // implement only the above methods!

    default public List<FlattenSolvableTuple<T>> getTupleList(boolean onlyone) {
        Vector<FlattenSolvableTuple<T>> result = new Vector<>();
        for (int x = 0 ; x < getWidth() ; ++x) {
            for (int y = 0; y < getHeight(); ++y) {
                List<FlattenSolvableTuple<T>> fstlist = getTuplesForCell(x, y);
                if (fstlist == null) continue;
                if (fstlist.size() == 0) continue;
                result.addAll(fstlist);
                if (onlyone) return result;
            }
        }
        return result;
    }




    @Override
    default public List<FlattenSolvableTuple<T>> getSuccessorTuples()
    {
        return getTupleList(false);
    }

    @Override
    default public List<T> guessAlternatives()
    {
        return getTupleList(true).get(0).choices;
    }
}
