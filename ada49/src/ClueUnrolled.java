import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/**
 * Created by chien on 11/12/2017.
 */

/* this class will take any single or double clue and unroll it to
 * all possible positions and rotations
 *
 *
 * there are two different position algorithms:
 * both algorithms start with a W cell in position 0 (North)
 * single clue:
 *   for i is 1 to 7: set cells 1 to i with B, all other cells W
 *  double clue:
 *  invariant: x1 + o1 + x2 + o2 = 7, x1,o1,x2 > 1, o2 > 0
 *    for x1 = 1 to (7 - 2)
 *      for o1 = 1 to (7 - x1 - 1)
 *        for x2 = 1 to (7 - x1 - o1)
 *          cells 1 to x1 = B, cells x1+1 to x1 + o1 = W, cells x1+o1+1 to x1+o1+x2 = B, all else = W
 *
 * then, in both cases, rotate each position discovered into all 8 rotations
 * and cull duplicates.
 *
 * also, as part of this process, if a clue is a specific number, limit the unrolled clues to only those
 * where the number matches (single: i matches clue, double: x1 matches first clue, x2 matches second)
 * (Rotations should allow match swapping of x1 and x2 clues)
 */

public class ClueUnrolled
{
    Set<String> clues = new HashSet<>();
    Vector<Integer> clue;

    public ClueUnrolled(Vector<Integer> clue)
    {
        this.clue = clue;
        if (clue.size() == 1) CalculateSingle(clue.get(0));
        else if (clue.size() == 2) CalculateDouble(clue.get(0),clue.get(1));
        else throw new RuntimeException("ClueUnrolled has too many clue elements!");

        ApplyRotations();

        if (clues.size() == 0) throw new RuntimeException("illegal clues to clueunrolled");
    }

    public ClueUnrolled(ClueUnrolled right)
    {
        clue = right.clue;
        clues.addAll(right.clues);
    }



    private void CalculateSingle(int c1)
    {
        // special case for single that has no white at all.
        if (c1 == 8 || c1 == -1)
        {
            clues.add("BBBBBBBB");
        }

        for (int i = 1 ; i <= 7 ; ++i)
        {
            if (c1 != -1 && c1 != i) continue;
            StringBuffer sb = new StringBuffer().append('W');
            for (int b1 = 0 ; b1 < i ; ++b1) sb.append('B');
            for (int w1 = 0 ; w1 < 7-i ; ++w1) sb.append('W');
            clues.add(sb.toString());
        }
    }

    private void CalculateDouble(int c1, int c2)
    {
        // no special case here...every double _must_ have at least 2 whites to separate
        // the two black areas from each other on both sides.
        for (int b1 = 1 ; b1 <= 7-2 ; ++b1)
        {
            if (c1 != -1 && c1 != b1) continue;
            for (int w1 = 1 ; w1 <= 7 - b1 - 1 ; ++w1)
            {
                for (int b2 = 1 ; b2 <= 7 - b1 - w1 ; ++b2)
                {
                    if (c2 != -1 && c2 != b2) continue;
                    int w2 = 7 - b1 - w1 - b2;
                    StringBuffer sb = new StringBuffer();
                    sb.append('W');
                    for (int i = 0 ; i < b1 ; ++i) sb.append('B');
                    for (int i = 0 ; i < w1 ; ++i) sb.append('W');
                    for (int i = 0 ; i < b2 ; ++i) sb.append('B');
                    for (int i = 0 ; i < w2 ; ++i) sb.append('W');
                    clues.add(sb.toString());
                }
            }
        }
    }






    private void ApplyRotations()
    {
        Set<String> old = clues;
        clues = new HashSet<>();

        for (String clue : old)
        {
            for (int i = 0 ; i < 8 ; ++i)
            {
                ApplyRotation(clue,i);
            }
        }
    }

    private void ApplyRotation(String clue,int rot)
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0 ; i < 8 ; ++ i) { sb.append(clue.charAt((i+rot)%8));  }
        clues.add(sb.toString());
    }


    public void Restrict(String s)
    {
        Set<String> newset = new HashSet<>();

        for (String obj: clues)
        {
            boolean bad = false;

            for (int i = 0; i < 8; ++i)
            {
                char oi = obj.charAt(i);
                char si = s.charAt(i);
                if (si == '.') continue;
                if (si != oi)
                {
                    bad = true;
                    break;
                }
            }

            if (!bad) newset.add(obj);
        }
        clues = newset;
    }

    public boolean isValid()
    {
        return clues.size() > 0;
    }

    public String intersection()
    {
        char[] isect = null;
        for (String obj : clues)
        {
            if (isect == null)
            {
                isect = obj.toCharArray();
                continue;
            }

            for (int i = 0 ; i < 8 ; ++i)
            {
                if (isect[i] != obj.charAt(i)) isect[i] = '.';
            }
        }

        return new String(isect);
    }

    public boolean matches(String s)
    {
        for (String obj : clues ) { if (s.equals(obj)) return true; }
        return false;
    }

}
