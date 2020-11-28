import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

public class CellContents {
    private SortedSet<Integer> possibles = new TreeSet<>();
    int minsize = 1;
    int maxsize = -1;
    public CellContents(int regionsize) { for (int i = 1 ; i <= regionsize ; ++i) possibles.add(i); maxsize = regionsize; }
    public CellContents(CellContents right) {possibles.addAll(right.possibles); }

    public boolean contains(int x) { return possibles.contains(x); }
    public void clear(int x) { possibles.remove(x); }
    public boolean set(int x) {
        if (!contains(x)) return false;
        possibles.clear();
        possibles.add(x);
        return true;
    }
    public int size() { return possibles.size(); }
    public Collection<Integer> getPossibles() { return possibles; }
    public int getLargest() { return possibles.last(); }
    public int getSmallest() { return possibles.first(); }
    public void removeEqualLarger(int v) { for (int i = v ; i <= maxsize ; ++i) clear(i); }
    public void removeEqualSmaller(int v) { for (int i = minsize ; i <= v ; ++i) clear(i); }

    @Override public String toString() {
        StringBuffer sb = new StringBuffer();
        possibles.stream().sorted().forEach(i->sb.append(i));
        return sb.toString();
    }
}
