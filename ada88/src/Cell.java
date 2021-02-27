import grid.puzzlebits.Direction;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Cell {
    private Set<Direction> arrows = new HashSet<>();

    public Cell() {
        for (Direction d : Direction.orthogonals()) arrows.add(d);
        arrows.add(null);
    }

    public Cell(Cell right) {
        arrows.addAll(right.arrows);
    }

    public boolean contains(Direction d) {
        return arrows.contains(d);
    }

    public void set(Direction d) {
        if (!contains(d)) throw new RuntimeException("Can't set to a missing arrow");
        arrows.clear();
        arrows.add(d);
    }

    public void clear(Direction d) {
        arrows.remove(d);
    }

    public int size() {
        return arrows.size();
    }

    // only sensical if size == 1
    public Direction getSolo() {
        return arrows.iterator().next();
    }

    public Collection<Direction> getContents() { return arrows; }
    public boolean isBlank() { return size() == 1 && arrows.contains(null); }
    public boolean isArrow() { return !arrows.contains(null); }


}
