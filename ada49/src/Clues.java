import java.util.Vector;

/**
 * Created by chien on 11/6/2017.
 */
public class Clues
{
    public static class VInt extends Vector<Integer> {}

    VInt[][] clues;

    public Clues(Board b)
    {
        clues = new VInt[b.getWidth()][b.getHeight()];
        b.forEachCell((x,y)-> {
            String s = b.gfr.getBlock("CLUES")[x][y];
            if (s.equals(".")) return;

            VInt nv = new VInt();
            clues[x][y] = nv;
            String[] subs = s.split("/");
            for (String sub : subs)
            {
                int v;
                if (sub.equals("?")) v = -1;
                else v = Integer.parseInt(sub);
                if (v == 0) throw new RuntimeException("illegal value in clues");
                else nv.add(v);
            }
        });
    }

}
