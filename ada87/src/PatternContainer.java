import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PatternContainer {
    int width;
    int height;
    private Map<Character,Pattern> patterns = new HashMap<>();

    public PatternContainer(int width,int height) {this.width = width; this.height = height;}
    public PatternContainer(PatternContainer right) {
        width = right.width;
        height = right.height;
        for(Character key : right.patterns.keySet()) {
            patterns.put(key,new Pattern(right.getPattern(key)));
        }
    }

    public Collection<Character> getPatternIds() { return patterns.keySet(); }
    public Pattern getPattern(char key) { return patterns.get(key); }
    public Pattern gocPattern(char key) {
        if (!patterns.containsKey(key)) {
            patterns.put(key,new Pattern(width,height));
        }
        return getPattern(key);
    }

    public void show() {
        for(char k : patterns.keySet()) {
            System.out.println("Pattern " + k + (patterns.get(k).isComplete() ? " is complete" : ""));
            patterns.get(k).show();
        }
    }

    public boolean isComplete() {
        return patterns.values().stream().allMatch(p->p.isComplete());
    }


}
