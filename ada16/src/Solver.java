
import grid.logic.flatten.FlattenLogicer;

import java.util.*;
import java.awt.Point;

public class Solver extends FlattenLogicer<Board>
{
	public Solver() {
		addLogicStep(new AntiTedium());
		addLogicStep(new TileConnectivity());
	}
}
		