import grid.puzzlebits.Direction;
import javafx.scene.control.Cell;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chien on 7/23/2017.
 */
public class CellState
{
    private Map<Direction,EdgeState> edges = new HashMap<>();

    public CellState()
    {
        for (Direction dir : Direction.orthogonals()) { edges.put(dir,EdgeState.UNKNOWN); }
    }

    public CellState(CellState right)
    {
        for (Direction dir : Direction.orthogonals()) { edges.put(dir,right.get(dir)); }
    }


    public EdgeState get(Direction d) { return edges.get(d); }
    public void set(Direction d,EdgeState es) { edges.put(d,es);}

}
