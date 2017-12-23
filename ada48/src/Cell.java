import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by chien on 10/1/2017.
 */
public class Cell
{
    private Set<Character> regions = new HashSet<>();
    boolean fixed = false;

    public Cell()
    {
    }

    public Cell(Cell right)
    {
        regions.addAll(right.regions);
        fixed = right.fixed;
    }

    public boolean isSingular() { return regions.size() == 1; }
    public boolean isEmpty() { return regions.isEmpty(); }
    public boolean canBe(char rid) { return regions.contains(rid); }
    public List<Character> getPossibles()
    {
        List<Character> result = new ArrayList<>();
        result.addAll(regions);
        return result;
    }

    public void setPossible(char rid) { regions.add(rid); }
    public void setImpossible(char rid) { regions.remove(rid); }
    public void setIs(char rid) { regions.clear(); regions.add(rid); fixed = true; }
    public boolean isFixed() { return fixed; }
}
