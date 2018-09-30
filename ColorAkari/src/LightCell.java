import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class LightCell {
    private Set<LightState> state = new HashSet<>();

    public LightCell() {
        state.addAll(Arrays.asList(LightState.values()));
    }

    public LightCell(LightCell right) { state.addAll(right.state); }

    public boolean isBroken() { return state.size() == 0; }
    public boolean isComplete() { return state.size() == 1; }
    public boolean isDark() { return isComplete() && state.contains(LightState.NOLIGHT); }
    public boolean isLight() { return !state.contains(LightState.NOLIGHT); }
    public boolean contains(LightState ls) { return state.contains(ls); }
    public LightState getSingle() { return state.iterator().next(); }
    public void setAs(LightState ls) { state.clear(); state.add(ls);}
    public void remove(LightState ls) { state.remove(ls); }
    public Stream<LightState> stream() { return state.stream(); }



}
