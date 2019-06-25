import grid.logic.flatten.FlattenLogicer;

import java.util.HashSet;
import java.util.Set;

public class Solver extends FlattenLogicer<Board> {
    private static class RegionPair {
        char small;
        char large;
        public RegionPair(char a,char b) {
            small = a < b ? a : b;
            large = a > b ? a : b;
        }
        public int hashCode() {
            return small * 31 + large + 37;
        }

        public boolean equals(Object right) {
            if (right == null) return false;
            if (!(right instanceof RegionPair)) return false;
            RegionPair rightrp = (RegionPair)right;
            return small == rightrp.small && large == rightrp.large;
        }
    }

    public Solver(Board b) {
        Set<RegionPair> pairs = new HashSet<>();

        b.forEachCell((x,y)-> {
            char thisid = b.getRegionId(x,y);
            if (x < b.getWidth() - 1) {
                char eastid = b.getRegionId(x+1,y);
                if (thisid != eastid) pairs.add(new RegionPair(thisid,eastid));
            }
            if (y < b.getHeight() - 1) {
                char southid = b.getRegionId(x,y+1);
                if (thisid != southid) pairs.add(new RegionPair(thisid,southid));
            }

            if (x == b.getWidth() - 1) return;
            if (y == b.getHeight() - 1) return;
            addLogicStep(new QuadCellLogicStep(x,y));
        });

        addLogicStep(new BlackConnectivityLogicStep());

        for (char c : b.regionsById.keySet()) {
            addLogicStep(new RegionLogicStep(c));
        }

        for (RegionPair rp : pairs) {
            addLogicStep(new RegionPairLogicStep(rp.small,rp.large));
        }




    }
}
