package grid.logic.flatten;

import java.util.List;
import java.util.Vector;

/**
 * Created by chien on 10/1/2017.
 *  This class implements a very standard behavior for this type of puzzle
 *  where there is at most one tuple created for each cell of a grid for successor tuples
 *  and a valid guess is the boards of the first non-null tuple we come across.
 *
 */
public abstract class StandardFlattenSolvable<T> extends MultiFlattenSolvable<T>
{
    abstract public int getWidth();
    abstract public int getHeight();

    // required for FlattenSolvable
    abstract public boolean isComplete();
    // applies a move object returned in a tuple returned by getOneTuple()
    // (required for FlattenSolvable)
    abstract public void applyMove(Object o);

    // returns null if this particular cell is already solved.
    abstract public FlattenSolvableTuple<T> getOneTuple(int x,int y);







    public List<FlattenSolvableTuple<T>> getTuplesForCell(int x,int y)
    {
        FlattenSolvableTuple<T> fst = getOneTuple(x,y);
        if (fst == null) return null;

        Vector<FlattenSolvableTuple<T>> result = new Vector<>();
        result.add(fst);
        return result;
    }

}
