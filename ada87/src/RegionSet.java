import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class RegionSet {
    private Set<RegionId> regions = new HashSet<>();

    public RegionSet(Collection<RegionId> regions) { this.regions.addAll(regions); }
    public RegionSet(RegionSet right) { this.regions.addAll(right.regions); }

    public boolean hasRegion(RegionId rid) { return regions.contains(rid); }
    public void clearRegion(RegionId rid) { regions.remove(rid); }
    public void setRegion(RegionId rid) {
        if (!hasRegion(rid)) throw new RuntimeException("Can't set a nonexistent region!");
        regions.clear();
        regions.add(rid);
    }
    public int size() { return regions.size(); }
    public Collection<RegionId> getRegions() { return regions; }
}
