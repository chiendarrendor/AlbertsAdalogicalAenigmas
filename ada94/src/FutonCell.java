import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FutonCell {
    private Map<Integer,FutonPair> pillows;
    private Map<Integer,FutonPair> futons;

    public FutonCell() {
        pillows = new HashMap<>();
        futons = new HashMap<>();

    }
    public FutonCell(FutonCell right) {
        pillows = FutonUtilities.futonPairMapDeepCopy(right.pillows);
        futons = FutonUtilities.futonPairMapDeepCopy(right.futons);
    }

    public void addPillow(FutonPair futon) { pillows.put(futon.getUuid(),futon); }
    public void addFuton(FutonPair futon) { futons.put(futon.getUuid(),futon); }

    public boolean isUnique() { return pillows.size() + futons.size() == 1; }
    public FutonPair getUniqueFuton() {
        if (pillows.size() == 1) return pillows.values().iterator().next();
        else return futons.values().iterator().next();
    }

    public void clearPillow(FutonPair fp) { pillows.remove(fp.getUuid()); }
    public void clearFuton(FutonPair fp) { futons.remove(fp.getUuid()); }

    public Collection<FutonPair> getPillows() { return pillows.values(); }
    public Collection<FutonPair> getFutons() { return futons.values(); }


}
