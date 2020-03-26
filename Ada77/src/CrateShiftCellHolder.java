import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CrateShiftCellHolder {
    private Point me;
    private Set<CrateShift> initials = new HashSet<>();
    private Set<CrateShift> terminals = new HashSet<>();
    private Set<CrateShift> intermediates = new HashSet<>();
    private boolean canBeEmpty = true;

    public Point getSelf() { return me; }

    public CrateShiftCellHolder(int x,int y) {me = new Point(x,y); }
    public CrateShiftCellHolder(CrateShiftCellHolder right) {
        me = right.me;
        initials.addAll(right.initials);
        terminals.addAll(right.terminals);
        intermediates.addAll(right.intermediates);
        canBeEmpty = right.canBeEmpty;
    }

    public void addInitial(CrateShift s) { initials.add(s); }
    public void addIntermediate(CrateShift s) { intermediates.add(s); }
    public void addTerminal(CrateShift s) { terminals.add(s); }
    public void removeInitial(CrateShift s) { initials.remove(s); }
    public void removeIntermediate(CrateShift s) { intermediates.remove(s); }
    public void removeTerminal(CrateShift s) { terminals.remove(s); }
    public boolean isOnBoard(CrateShift s) { return initials.contains(s); }

    public int initialSize() { return initials.size(); }
    public int terminalSize() { return terminals.size(); }
    public int intermediateSize() { return intermediates.size(); }
    public CrateShift getUniqueInitial() { return initials.iterator().next(); }
    public CrateShift getUniqueTerminal() { return terminals.iterator().next(); }
    public CrateShift getUniqueIntermediate() { return intermediates.iterator().next(); }
    public Collection<CrateShift> getTerminals() { return terminals; }

    public Set<CrateShift> getAll() {
        Set<CrateShift> result = new HashSet<>();
        result.addAll(initials);
        result.addAll(terminals);
        result.addAll(intermediates);
        return result;
    }
}
