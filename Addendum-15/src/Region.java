import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Region {
    private Set<Point> cells = new HashSet<>();
    private Set<Point> enemies = new HashSet<>();
    int size;
    private int actualsize = -1;
    int id;

    public Region(int size, int id) {
        this.size = size;
        this.id = id;
        if (size == 1) actualsize = 2; // this is the only case where we might get a 0 or negative size.
    }

    public Region(Region right) {
        id = right.id;
        size = right.size;
        actualsize = right.actualsize;
        cells.addAll(right.cells);
        enemies.addAll(right.enemies);
    }

    public boolean hasEnemy(Point p) { return enemies.contains(p); }
    public boolean hasCell(Point p) { return cells.contains(p); }
    public Set<Point> cellSet() { return cells; }
    public Iterable<Point> cellIter() { return cells; }
    public Stream<Point> cellStream() { return cells.stream(); }
    public Stream<Point> enemyStream() { return enemies.stream(); }

    public int getId() { return id; }
    public boolean isActive() { return cells.size() > 0; }
    public boolean isDone() {
        if (!isActive()) return true;
        if (actualsize == -1) return false;
        return cells.size() == actualsize;
    }

    public void setActualSize(int newsize) { actualsize = newsize; }
    public void addEnemy(Point p) { enemies.add(p); }
    public void addCell(Point p) { cells.add(p); }
    public void emptyCells() { cells.clear(); }

    public int getActualSize() { return actualsize; }
    public int getSize() { return size; }
    public int numCells() { return cells.size(); }

    public int smaller() { return actualsize == -1 ? size - 1 : actualsize; }
    public int larger() { return actualsize == -1 ? size + 1 : actualsize; }



    public boolean numbersMatch(Region right) {
        if (actualsize == -1 && right.actualsize == -1) {
            if (size == right.size) return true;
            if (size + 2 == right.size) return true;
            if (size - 2 == right.size) return true;
            return false;
        }
        if (actualsize == -1) {
            if (size + 1 == right.actualsize) return true;
            if (size - 1 == right.actualsize) return true;
            return false;
        }
        if (right.actualsize == -1) {
            if (right.size + 1 == actualsize) return true;
            if (right.size - 1 == actualsize) return true;
            return false;
        }

        return actualsize == right.actualsize;
    }

    public int joinActualSize(Region right) {
        if (actualsize != -1) return actualsize;
        else if (right.actualsize != -1) return right.actualsize;
        else if (getSize() != right.getSize()) return (getSize() + right.getSize())/2;
        return -1;
    }




    private Set<Point> oneAdjacent(Point p) {
        Set<Point> result = new HashSet<>();
        for(Direction d: Direction.orthogonals()) result.add(d.delta(p,1));
        return result;
    }



    // returns a list of points representing cells that are:
    // 1) on the board
    // 2) not in this region
    // 3) extendable into
    public Set<Point> getAdjacents(Board b,boolean extendable) {
        return cells.stream()
                .flatMap(p->oneAdjacent(p).stream())
                .filter(p->b.onBoard(p.x,p.y))
                .filter(p->!cells.contains(p))
                .filter(p->extendable ? b.canExtendInto(getId(),p) : true)
                .collect(Collectors.toSet());
    }


    @Override public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append('#').append(getId()).append("#");
        sb.append(getActualSize()).append('(').append(getSize()).append(')');
        cells.stream().forEach(p->sb.append(p.toString()));
        return sb.toString();
    }


}
