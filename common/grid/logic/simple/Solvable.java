package grid.logic.simple;

import java.util.List;

public interface Solvable<T>
{
    boolean isSolution();
    List<T> guessAlternatives();
}