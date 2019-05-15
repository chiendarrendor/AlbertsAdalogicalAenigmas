import java.util.HashSet;
import java.util.Set;

public class Possibles {
    private Set<Integer> indexes = new HashSet<>();

    public Possibles() {
        for (int i = 0 ; i < Region.templates.length; ++i) indexes.add(i);
    }

    public Possibles(Possibles right) { indexes.addAll(right.indexes); }

    public boolean isPossible(int index) { return indexes.contains(index); }
    public void deny(int index) { indexes.remove(index); }
    public void set(int index) {
        if (!isPossible(index)) throw new RuntimeException("Violation of Possible!");
        indexes.clear();
        indexes.add(index);
    }

    public Set<Integer> possibles() { return indexes; }


}
