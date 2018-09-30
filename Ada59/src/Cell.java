import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class Cell {
    public static int WALLID = -1;
    private Set<Integer> contents = new HashSet<>();
    public Cell(int max) {
        for (int i = 1 ; i <= max; ++i) contents.add(i);
        contents.add(WALLID);
    }

    public Cell(Cell right) {
        contents.addAll(right.contents);
    }

    public boolean contains(int x) { return contents.contains(x); }
    public Stream<Integer> stream() { return contents.stream(); }
    public void remove(int x) { contents.remove(x); }
    public void removeAllBut(int x) { contents.clear(); contents.add(x); }
    public void makeWall() { contents.clear(); contents.add(WALLID); }
    public void makeNotWall() { remove(WALLID); }
    public boolean isWall() { return contents.size() == 1 && contents.contains(WALLID); }
    public boolean canBeWall() { return contents.contains(WALLID); }
    public boolean isNotWall() { return !contents.contains(WALLID); }
    // this method will only return something sensical if you know that this cell isComplete
    public int getSingleNumber() { return contents.iterator().next(); }
    public int getOptionCount() { return contents.size(); }
    public boolean isComplete() {
        if (contents.size() == 1) return true;
        return false;
    }
    public boolean isBroken() { return contents.size() == 0; }




}
