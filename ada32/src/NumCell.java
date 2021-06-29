
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class NumCell {
    private Set<Integer> numbers = new HashSet<>();

    public NumCell(boolean isBlank) {
        for (int i = 0 ; i <= 5 ; ++i) numbers.add(i);
        if (isBlank) set(0);
    }

    public NumCell(NumCell right) { numbers.addAll(right.numbers); }

    public boolean isBroken() { return numbers.size() == 0; }
    public boolean isDone() { return numbers.size() == 1; }
    public int getComplete() { return numbers.iterator().next(); }

    public boolean doesContain(int v) { return numbers.contains(v); }

    public void set(int v) {
        if (!doesContain(v)) throw new RuntimeException("Can't set a NumCell to a missing number!");
        numbers.clear();
        numbers.add(v);
    }

    public void remove(int v) {
        numbers.remove(v);
    }

    public Collection<Integer> getPossibles() { return numbers; }
}
