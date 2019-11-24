import java.util.HashSet;
import java.util.Set;

public class TileSet {
    Set<Tile> set = new HashSet<>();

    public TileSet() {}
    public TileSet(TileSet right) {
        set.addAll(right.set);
    }
    public void add(Tile t) { set.add(t); }
}
