import java.util.HashSet;

public class RectangleRegionSet extends HashSet<RectangleRegion> {

    public RectangleRegionSet() {}

    public RectangleRegionSet(RectangleRegionSet right) {
        addAll(right);
    }
}
