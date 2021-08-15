import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Cell {
    private Set<CellType> contents = new HashSet<>();

    public Cell(boolean isPillar) {
        if (isPillar) {
            contents.add(CellType.PILLAR);
        } else {
            contents.add(CellType.AISLE);
            contents.add(CellType.PILLOW);
            contents.add(CellType.FUTON);
        }
    }

    public Cell(Cell right) {
        contents.addAll(right.contents);
    }

    public boolean isDone() { return contents.size() == 1; }
    public CellType getDoneType() { return contents.iterator().next(); }

    public boolean isValid() { return contents.size() > 0; }

    public boolean has(CellType ct) { return contents.contains(ct); }
    public void set(CellType ct) {
        if (!has(ct)) throw new RuntimeException("Can't set to something it doesn't has");
        contents.clear();
        contents.add(ct);
    }
    public void clear(CellType ct) { contents.remove(ct); }
    public Collection<CellType> getPossibles(){ return contents; }


}
