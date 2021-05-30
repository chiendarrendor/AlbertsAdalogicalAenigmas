import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CellState {
    Set<Integer> ints = new HashSet<>();
    public CellState(int size) {
        for (int i = 1 ; i <= size ; ++i) ints.add(i);
    }
    public CellState(CellState right) {
        ints.addAll(right.ints);
    }

    public boolean complete() { return ints.size() == 1; }
    public int unique() { return ints.iterator().next(); }
    public boolean broken() { return ints.size() == 0; }
    public boolean contains(int v) { return ints.contains(v); }
    public void remove(int v) { ints.remove(v); }
    public void set(int v) {
        if (!contains(v)) throw new RuntimeException("Don't set something not here!");
        ints.clear();
        ints.add(v);
    }

    public Collection<Integer> getSet() { return ints; }
}
