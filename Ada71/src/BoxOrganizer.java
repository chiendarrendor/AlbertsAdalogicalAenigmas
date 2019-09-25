import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class BoxOrganizer {
    public List<Rectangle> boxes;
    private Map<Rectangle,Point> tierxy = new HashMap<>();

    public interface RectangleInfo {
        int op(Rectangle r);
    }


    public BoxOrganizer(List<Rectangle> boxes) {
        this.boxes = boxes;
        Map<Rectangle,Integer> xtiers = makeTiers(r->r.x);
        Map<Rectangle,Integer> ytiers = makeTiers(r->r.y);

        for(Rectangle r : boxes) {
            tierxy.put(r,new Point(xtiers.get(r),ytiers.get(r)));
        }
    }

    public Point getTierInfo(Rectangle r) { return tierxy.get(r); }




    private Map<Rectangle,Integer> makeTiers(RectangleInfo ri) {
        TreeMap<Integer,List<Rectangle>> tiers = new TreeMap<>();

        for (Rectangle r : boxes) {
            int v = ri.op(r);
            if (!tiers.containsKey(v)) {
                tiers.put(v,new ArrayList<>());
            }
            tiers.get(v).add(r);
        }

        Map<Rectangle,Integer> result = new HashMap<>();
        int tidx = 0;
        for (int tval : tiers.keySet() ) {
            for (Rectangle r : tiers.get(tval)) {
                result.put(r,tidx);
            }
            ++tidx;
        }


        return result;
    }
}
