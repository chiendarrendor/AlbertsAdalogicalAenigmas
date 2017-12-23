import java.util.HashMap;

/**
 * Created by chien on 4/22/2017.
 */
public class Regions extends HashMap<Character,Region>
{
    public Regions(Board board)
    {
        for (int x = 0 ; x < board.getWidth() ; ++x)
        {
            for (int y = 0 ; y < board.getHeight() ; ++y)
            {
                char rid = board.getRegionId(x,y);
                Region r = null;

                if (!containsKey(rid))
                {
                    r = new Region(rid);
                    put(rid,r);
                }
                else
                {
                    r = get(rid);
                }
                r.addCell(x,y);
                if (board.hasNumber(x,y)) r.setNumber(Integer.parseInt(""+board.getNumber(x,y)));
            }
        }
    }
}
