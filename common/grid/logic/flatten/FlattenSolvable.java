package grid.logic.flatten;

import java.util.List;

/**
 * Created by chien on 5/20/2017.
 */
public interface FlattenSolvable<T>
{
    public boolean isComplete();
    public List<FlattenSolvableTuple<T>> getSuccessorTuples();
    public void applyMove(Object o);
    List<T> guessAlternatives();
}
