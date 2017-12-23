package grid.logic.astar;

import java.util.List;

/**
 * Created by chien on 5/20/2017.
 */
public interface AStarSolvable<T>
{
    public int winGrade();
    public int grade();
    public List<T> successors();
    public String canonicalKey();
}
