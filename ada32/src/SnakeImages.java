import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class SnakeImages {
    private List<List<Point>> images = new ArrayList<>();

    private void recurse(List<Point> prefix) {
        if (prefix.size() == 5) {
            images.add(prefix);
            return;
        }

        Point cur = prefix.get(prefix.size()-1);

        for (Direction d: Direction.orthogonals()) {
            Point np = d.delta(cur.x, cur.y, 1);
            if (prefix.stream().anyMatch(q -> q.equals(np))) continue;
            List<Point> nl = new ArrayList<>();
            nl.addAll(prefix);
            nl.add(np);
            recurse(nl);
        }
    }

    public SnakeImages() {

        List<Point> start = new ArrayList<>();
        start.add(new Point(0,0));

        recurse(start);


        System.out.println("# of snakes: " + images.size());
    }


}
