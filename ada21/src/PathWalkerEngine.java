import grid.puzzlebits.Direction;
import grid.puzzlebits.Turns;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PathWalkerEngine {
    List<PathWalker> queue = new ArrayList<>();
    String solution = null;

    public String getSolution() { return solution; }

    public PathWalkerEngine(Board b) {
        String[] startbits = b.getVar("START").split(" ");
        String[] endbits = b.getVar("END").split(" ");
        String[] dirbits = b.getVar("PATHORDER").split(" ");
        Turns[] turns = new Turns[dirbits.length];
        for (int i = 0 ; i < turns.length ; ++i) {
            char c = dirbits[i].charAt(0);
            switch(c) {
                case 'S': turns[i] = Turns.STRAIGHT; break;
                case 'L': turns[i] = Turns.LEFT; break;
                case 'R': turns[i] = Turns.RIGHT; break;
                default: throw new RuntimeException("Unknown Turn char " + c);
            }
        }


        Point start = new Point(Integer.parseInt(startbits[0]),Integer.parseInt(startbits[1]));
        Point end = new Point(Integer.parseInt(endbits[0]),Integer.parseInt(endbits[1]));
        Direction startdir = Direction.fromShort(startbits[2]);

        PathWalker startpw = new PathWalker(b,turns,start,startdir);
        queue.add(startpw);


        while(queue.size() > 0) {
            PathWalker curpw = queue.get(queue.size() - 1);
            queue.remove(queue.size() - 1);
            if (curpw.getPoint().equals(end)) {
                solution = curpw.getSolution();
                break;
            }

            List<PathWalker> successors = curpw.walk();
            Collections.reverse(successors);
            queue.addAll(successors);

        }

    }

}
