import java.awt.Point;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Knight {
    private Point initial;
    private static int nextId = 0;
    private int id;

    private int zeroId = -1;
    private Map<Integer,KnightsPath> paths = new HashMap<>();
    private int movecount;
    private boolean isLocked;


    private void recursive(Board b, KnightsPath parent, int depth) {
        if (movecount == depth || movecount == -1) {
            paths.put(parent.getId(),parent);
            if (parent.size() == 1) zeroId = parent.getId();
        }
        if (movecount != -1 && movecount == depth) return;

        // if we get here, we get to go deeper.

        for (Point p : KnightsJump.destinations(parent.tail())) {
            if (!b.inBounds(p.x,p.y)) continue;
            if (b.isKnight(p.x,p.y)) continue;
            if (!parent.addable(p)) continue;
            KnightsPath newPath = new KnightsPath(parent);
            newPath.add(p);
            recursive(b,newPath,depth+1);
        }
    }

    // movecount is -1 if knight does not have a numeric clue
    public Knight(Board b,Point p, int movecount) {
        isLocked = false;
        initial = p;
        id = ++nextId;
        this.movecount = movecount;
        recursive(b,new KnightsPath(p),0);

        System.out.println("Knight at " + p + " has " + paths.size() + " paths");
    }

    public Knight(Knight right) {
        isLocked = right.isLocked;
        initial = right.initial;
        id = right.id;
        movecount = right.movecount;
        zeroId = right.zeroId;
        for (int key : right.paths.keySet()) {
            paths.put(key,right.paths.get(key));
        }
    }

    public int numPaths() { return paths.size(); }
    public void showPaths() {
        for (KnightsPath kp : paths.values()) {
            System.out.println(kp);
        }
    }

    public boolean hasPathWithKey(int key) { return paths.containsKey(key); }
    public void clearPath(int key) {
        if (key == zeroId) zeroId = -1;
        paths.remove(key);
    }

    public void setPath(int key) {
        if (!hasPathWithKey(key)) throw new RuntimeException("Knight doesn't contain this path");
        KnightsPath kp = paths.get(key);
        paths.clear();
        paths.put(key,kp);
        if (zeroId != key) zeroId = -1;
    }

    public Collection<KnightsPath> getPaths() { return paths.values(); }
    public Collection<Integer> getPathIds() { return paths.keySet(); }
    public boolean hasZero() { return zeroId >= 0; }
    public int getId() { return id; }
    public int zeroId() { return zeroId; }
    public Point getInitial() { return initial; }
    public void lock() { isLocked = true; }
    public boolean isLocked() { return isLocked; }
    public KnightsPath getPath(int pathid) { return paths.get(pathid); }


}
