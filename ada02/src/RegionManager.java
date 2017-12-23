import grid.lambda.CellLambda;
import grid.puzzlebits.Direction;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by chien on 10/22/2017.
 */
public class RegionManager
{
    private Map<Character,Region> regions = new HashMap<>();

    public RegionManager(RegionSelector rs)
    {
        CellLambda.forEachCell(rs.getWidth(),rs.getHeight(),(x,y)->{
            char rid = rs.getRegionId(x,y);
            if (!regions.containsKey(rid))
            {
                regions.put(rid,new Region());
            }
            Region r = regions.get(rid);
            r.addCell(x,y);
        });

        CellLambda.forEachCell(rs.getWidth(),rs.getHeight(),(x,y)->{
           char rid = rs.getRegionId(x,y);

           if (x < rs.getWidth() - 1)
           {
               char orid = rs.getRegionId(x+1,y);
               if (rid != orid)
               {
                   regions.get(rid).addEdge(x,y, Direction.EAST);
                   regions.get(orid).addEdge(x+1,y,Direction.WEST);
               }
           }

            if (y < rs.getHeight() - 1)
            {
                char orid = rs.getRegionId(x,y+1);
                if (rid != orid)
                {
                    regions.get(rid).addEdge(x,y, Direction.SOUTH);
                    regions.get(orid).addEdge(x,y+1,Direction.NORTH);
                }
            }
        });
    }

    public Set<Character> getRegionIds()
    {
        return regions.keySet();
    }

    public Region get(char regionid)
    {
        return regions.get(regionid);
    }
}
