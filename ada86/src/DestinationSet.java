import java.util.HashSet;
import java.util.Set;

// coontains a list of possibles for a given Cell
public class DestinationSet {
    public Set<PossibleDestination> destinations = new HashSet<>();

    public DestinationSet() {}
    public DestinationSet(DestinationSet right) {
        destinations.addAll(right.destinations);
    }

    public void add(PossibleDestination pd) { destinations.add(pd); }
    public int size() { return destinations.size(); }
    public PossibleDestination getOne() { return destinations.iterator().next(); }
    public boolean contains(PossibleDestination pd) { return destinations.contains(pd); }
    public void remove(PossibleDestination pd) { destinations.remove(pd); }
}
