import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CellSet implements Iterable<Integer> {
    SortedSet<Integer> numbers = new TreeSet<>();

    public CellSet(int max) {
        IntStream.rangeClosed(1,max).forEach(i->numbers.add(i));
    }
    public CellSet(CellSet right) { numbers.addAll(right.numbers); }

    public int size() { return numbers.size(); }
    public Stream<Integer> stream() { return numbers.stream(); }
    public Iterator<Integer> iterator() { return numbers.iterator(); }

    // only should be used on CellSets where size == 1
    public int theNumber() { return numbers.iterator().next(); }

    public boolean is(int v) {
        if (!numbers.contains(v)) return false;
        numbers.clear();
        numbers.add(v);
        return true;
    }
    public boolean has(int v) {
        return numbers.contains(v);
    }

    public void isNot(int v) {
        numbers.remove(v);
    }

    @Override public String toString() {
        return numbers.stream().map(i->i.toString()).collect(Collectors.joining(""));
    }


}
