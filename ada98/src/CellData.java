import grid.puzzlebits.Direction;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CellData {
    int x;
    int y;
    private Set<Integer> numbers = new HashSet<>();


    public CellData(int x,int y,int minnumber, int maxnumber) {
        this.x = x;
        this.y = y;
        for (int i = minnumber ; i <= maxnumber; ++i) numbers.add(i);
    }

    public CellData(CellData right) {
        x = right.x;
        y = right.y;
        numbers.addAll(right.numbers);
    }


    public boolean isValid() { return numbers.size() > 0; }
    public boolean isComplete() { return numbers.size() == 1; }
    public int getValue() { return numbers.iterator().next(); }

    public Collection<Integer> possibles() { return numbers; }
    public boolean has(int v) { return numbers.contains(v); }
    public void clear(int v) { numbers.remove(v); }
    public void set(int v) {
        if (!has(v)) throw new RuntimeException("Can't Set what it doesn't has!");
        numbers.clear();
        numbers.add(v);
    }
    public int largest() {
        return numbers.stream().mapToInt(v->v).max().getAsInt();
    }
    public int smallest() {
        return numbers.stream().mapToInt(v->v).min().getAsInt();
    }



}
