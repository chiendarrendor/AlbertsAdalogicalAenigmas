package grid.puzzlebits;

import grid.puzzlebits.CanonicalPointSet;

import java.awt.Point;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PentominoData {
    private static Map<Character,String[]> pentominoes = new HashMap<>();

    static {
        pentominoes.put('F', new String[]{
                ".*.",
                "***",
                "*.."
        });

        pentominoes.put('I', new String[]{
                "*****"
        });

        pentominoes.put('L', new String[]{
                "****",
                "*..."
        });

        pentominoes.put('N', new String[]{
                "**..",
                ".***"
        });

        pentominoes.put('P', new String[]{
                "***",
                "**."
        });

        pentominoes.put('T', new String[]{
                "***",
                ".*.",
                ".*."
        });

        pentominoes.put('U', new String[]{
                "*.*",
                "***"
        });

        pentominoes.put('V', new String[]{
                "..*",
                "..*",
                "***"
        });

        pentominoes.put('W', new String[]{
                "..*",
                ".**",
                "**."
        });

        pentominoes.put('X', new String[]{
                ".*.",
                "***",
                ".*."
        });

        pentominoes.put('Y', new String[]{
                "****",
                ".*.."
        });

        pentominoes.put('Z', new String[]{
                "**.",
                ".*.",
                ".**"
        });
    }

    private Map<String,Character> identifier = new HashMap<>();
    private Set<Point> makePointSet(String[] ary) {
        Set<Point> result = new HashSet<>();
        int width = -1;

        for (int y = 0 ; y < ary.length ; ++y) {
            if (width == -1) width = ary[y].length();
            else if (width != ary[y].length()) throw new RuntimeException("Width Mismatches in PentominoData");
            for (int x = 0; x < width ; ++x) {
                if (ary[y].charAt(x) != '*') continue;
                result.add(new Point(x,y));
            }
        }
        return result;
    }

    private void fillIdentifier(char name,Set<Point> points) {
        CanonicalPointSet cps = new CanonicalPointSet(points);
        for(String s: cps.equivalances()) {
            identifier.put(s,name);
        }
    }


    public PentominoData() {
        for(Map.Entry<Character,String[]> ent : pentominoes.entrySet()) {
            fillIdentifier(ent.getKey(),makePointSet(ent.getValue()));
        }
    }

    public char getType(Collection<Point> points) {
        CanonicalPointSet cps = new CanonicalPointSet(points);
        String uid = cps.toString();
        if (!identifier.containsKey(uid)) throw new RuntimeException("Not a Pentomino!");
        return identifier.get(uid);
    }

    public Collection<Character> getNames() { return pentominoes.keySet(); }

}
