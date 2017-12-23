import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Created by chien on 4/24/2017.
 */
public class TurnDeterminer
{
    private static boolean isStraight(Point p0,Point p1,Point p2)
    {
        if (p0.x == p2.x || p0.y == p2.y) return true;
        return false;
    }

    public static List<AStarPath> run(List<AStarPath> paths)
    {
        int length = paths.get(0).pathLen();
        Vector<AStarPath> curPaths = new Vector<>();
        curPaths.addAll(paths);

        for (int i = 0 ; i < length ; ++i)
        {
            Map<Point,Vector<AStarPath>> divvy = new HashMap<>();
            for (AStarPath asp : curPaths)
            {
                Point curp = asp.path.get(i);
                if (!divvy.containsKey(curp)) divvy.put(curp,new Vector<AStarPath>());
                divvy.get(curp).add(asp);
            }
            if (divvy.size() == 1) continue;
            Point crux = curPaths.get(0).path.get(i-1);
            Point prev = curPaths.get(0).path.get(i-2);

            Point straightp = null;

            for (Point dir : divvy.keySet())
            {
                if (!isStraight(prev,crux,dir)) continue;
                straightp = dir;
            }

            if (straightp == null) throw new RuntimeException("No Straight given Choice!");
            curPaths = divvy.get(straightp);


        }
        return curPaths;
    }
}
