import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.IntStream;

public class Cell {
    private SortedSet<Integer> contents = new TreeSet<>();

    public Cell() { IntStream.rangeClosed(1,9).forEach(i->contents.add(i)); }
    public Cell(Cell right) { contents.addAll(right.contents); }
    public boolean has(int v) { return contents.contains(v); }
    public void set(int v) {
        if (!has(v)) throw new RuntimeException("Can't call set on set that doesn't has");
        contents.clear();
        contents.add(v);
    }
    public void clear(int v) {contents.remove(v); }
    public Collection<Integer> contents() { return contents; }
    public boolean isDone() { return contents.size() == 1; }
    public boolean isValid() { return contents.size() > 0; }
    public int smallest() { return contents.first(); }
    public int largest() { return contents.last(); }
    // only valid if isDone
    public int getValue() { return contents.iterator().next();  }
}
