import grid.letter.LetterRotate;
import grid.puzzlebits.Direction;
import grid.puzzlebits.Turns;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PathWalker {
    Board b;
    Point curcell;
    Direction entrydirection;
    Set<Point> seenpoints = new HashSet<>();
    StringBuffer buf = new StringBuffer();
    StringBuffer raw = new StringBuffer();
    int numcount = 0;
    Turns[] turnorder;

    private void processDot() {
        if (!b.isDot(curcell.x,curcell.y)) {
            raw.append("(" + curcell.x + "," + curcell.y + " " + entrydirection.getSymbol() + ")/");
            return;
        }
        numcount += b.getNumber(curcell.x,curcell.y);
        buf.append(LetterRotate.Rotate(b.getLetter(curcell.x,curcell.y),numcount % 10));
        raw.append(b.getNumber(curcell.x,curcell.y)).append(b.getLetter(curcell.x,curcell.y))
                .append(entrydirection.getSymbol()).append('/');
    }

    public String getSolution() { return buf.toString(); }
    public Point getPoint() { return curcell; }
    public String getRaw() { return raw.toString(); }


    public PathWalker(Board b, Turns[] turnorder, Point start, Direction dir) {
        this.b = b;
        this.turnorder = turnorder;
        update(start,dir);
    }

    public void update(Point next,Direction dir) {
        curcell = next;
        entrydirection = dir;
        seenpoints.add(next);
        processDot();
    }

    public PathWalker(PathWalker right) {
        b = right.b;
        curcell = right.curcell;
        entrydirection = right.entrydirection;
        seenpoints.addAll(right.seenpoints);
        buf.append(right.buf.toString());
        numcount = right.numcount;
        raw.append(right.raw.toString());
        turnorder = right.turnorder;
    }

    public List<PathWalker> walk() {
        List<PathWalker> result = new ArrayList<>();
        Arrays.stream(turnorder).forEach(t->walkOneDir(result,t.exitDir(entrydirection)));
        return result;
    }

    private void walkOneDir(List<PathWalker> list, Direction d) {
        if (b.getEdge(curcell.x,curcell.y,d) != EdgeState.PATH) return;
        Point next = d.delta(curcell,1);
        if (seenpoints.contains(next)) return;
        PathWalker newpw = new PathWalker(this);
        newpw.update(next,d);
        list.add(newpw);
    }
}
