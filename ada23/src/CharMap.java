import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// this class manages the relationship between a single letter cipher and
// the possible distances that could represent
public class CharMap
{
    private Map<Character,HashSet<Integer>> info = new HashMap<>();
    public CharMap() {}

    public CharMap(CharMap right)
    {
        right.info.keySet().stream().forEach((x)->info.put(x,new HashSet<>()));
        right.info.keySet().stream().forEach((letter)->{right.info.get(letter).stream().forEach((number)->info.get(letter).add(number));});
    }

    public void addEntry(char letter,int dist) {
        if (!info.containsKey(letter)) info.put(letter,new HashSet<>());
        info.get(letter).add(dist);
    }

    public Set<Integer> getEntrySet(char letter) { return info.get(letter); }
    public Set<Character> getCiphers() { return info.keySet(); }


    public void show() {
        info.keySet().stream().forEach((x)->{
            System.out.println("Cipher: " + x);
            info.get(x).stream().forEach((y)->System.out.println("  " + y));
        });
    }


}
