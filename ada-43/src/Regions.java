import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chien on 5/6/2017.
 */
public class Regions extends HashMap<Character, Region>
{
    public void addCellToRegion(char regionId, int x,int y)
    {
        if (!containsKey(regionId))
        {
            put(regionId,new Region(regionId));
        }
        get(regionId).addCell(x,y);
    }
}
