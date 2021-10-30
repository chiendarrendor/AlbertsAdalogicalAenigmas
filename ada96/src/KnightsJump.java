import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class KnightsJump {
    public static List<Point> destinations(Point start) {
        List<Point> result = new ArrayList<>();

        for (Direction d: Direction.orthogonals()) {
            Point inter = d.delta(start,2);
            result.add(d.right().delta(inter,1));
            result.add(d.right().getOpp().delta(inter,1));
        }
        return result;
    }
}
