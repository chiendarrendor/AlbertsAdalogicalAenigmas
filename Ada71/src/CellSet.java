import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class CellSet implements Iterable<Integer> {
    public static final int BLACK = 0;

    private Set<Integer> possibles = new HashSet<>();

    public CellSet(int max) {
        for (int i = 0 ; i <= max ; ++i) possibles.add(i);
    }
    public CellSet(CellSet right) {
        possibles.addAll(right.possibles);
    }

    boolean isSolo() { return possibles.size() == 1; }
    int getSolo() { return possibles.iterator().next(); }

    boolean has(int x) { return possibles.contains(x); }
    void set(int x) { possibles.clear(); possibles.add(x); }
    void remove(int x) { possibles.remove(x); }
    int count() { return possibles.size(); }

    @Override public Iterator<Integer> iterator() { return possibles.iterator(); }
}
