import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ActiveFenceSet {
    private Map<Integer,EdgeType> fencestate = new HashMap<>();
    int unknowns;


    public ActiveFenceSet(FenceDomain fences) {
        for (int f : fences.fences.keySet()) fencestate.put(f,EdgeType.UNKNOWN);
        unknowns = fences.fences.keySet().size();
    }

    public ActiveFenceSet(ActiveFenceSet right) {
        for (int f : right.fencestate.keySet()) fencestate.put(f,right.fencestate.get(f));
        unknowns = right.unknowns;
    }

    public EdgeType getFence(int fenceid) { return fencestate.get(fenceid); }
    public void setFence(int fenceid,EdgeType et) { fencestate.put(fenceid,et); --unknowns; }
    public int getUnknownCount() { return unknowns; }
    public Collection<Integer> getFenceIds() { return fencestate.keySet(); }
}
