package grid.logic.flatten;

import java.util.List;

/**
 * Created by chien on 5/20/2017.
 */
public interface FlattenSolvable<T>
{
    public boolean isComplete();
    public List<FlattenSolvableTuple<T>> getSuccessorTuples();
    // returns false if this move cannot be applied to the board
    // (may be needed if the moves in the tuples affect each other, i.e. a cell tuple involves the cell edges)
    public boolean applyMove(Object o);
    List<T> guessAlternatives();
}
