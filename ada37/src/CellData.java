import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CellData {
    Set<Integer> values = new HashSet<>();

    public final static int WALLVALUE = 0;

    public CellData() {
        for (int i = 0 ; i <= 4 ; ++i) values.add(i);
    }

    public CellData(CellData right) {
        values.addAll(right.values);
    }

    public Collection<Integer> values() { return values; }
    public boolean has(int v) { return values.contains(v); }
    public void clear(int v) { values.remove(v); }
    public void set(int v) {
        if (!has(v)) throw new RuntimeException("Can't set what don't has!");
        values.clear();
        values.add(v);
    }
    public boolean isValid() { return values.size() > 0; }
    public boolean isComplete() { return values.size() == 1; }
    public int getValue() { return values.iterator().next(); }
    public boolean isWall() { return isComplete() && getValue() == WALLVALUE; }
    public boolean isPath() { return isValid() && !has(WALLVALUE); }
}
