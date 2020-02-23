import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Cell {
    private Set<CellShape> possibleshapes = new HashSet<>();

    public Cell() {
        possibleshapes.addAll(Arrays.asList(CellShape.values()));
    }

    public Cell(Cell right) {
        possibleshapes.addAll(right.possibleshapes);
    }

    public boolean isEmpty() { return possibleshapes.size() == 0; }
    public boolean isDone() { return possibleshapes.size() == 1; }
    public boolean hasPossible(CellShape cs) { return possibleshapes.contains(cs); }

    // these two methods only make sense if isDone is true
    public boolean isBlank() { return hasPossible(CellShape.BLANK); }
    public CellShape getShape() { return possibleshapes.iterator().next(); }

    public void remove(CellShape cs) { possibleshapes.remove(cs); }
    public boolean set(CellShape cs) {
        if (!hasPossible(cs)) return false;
        possibleshapes.clear();
        possibleshapes.add(cs);
        return true;
    }
}
