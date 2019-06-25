import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.PointAdjacency;
import sun.rmi.runtime.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RegionPairLogicStep implements LogicStep<Board> {
    char regionida;
    char regionidb;

    public RegionPairLogicStep(char a, char b) { regionida = a; regionidb = b; }

    private static boolean adjacent(Region.OverlappingOmino ooa, Region.OverlappingOmino oob) {
        return PointAdjacency.adjacentToAny(ooa.setcells,oob.setcells,false);
    }

    private static LogicStatus oneToMany(Region.OverlappingOmino known,Region unknown) {
        List<Region.OverlappingOmino> stillvalid = new ArrayList<>();
        LogicStatus result = LogicStatus.STYMIED;
        for (Region.OverlappingOmino oot : unknown.overlaps) {
            if (adjacent(known,oot) && known.omino.mirrorfamily == oot.omino.mirrorfamily) {
                result = LogicStatus.LOGICED;
                continue;
            }
            stillvalid.add(oot);
        }
        unknown.overlaps = stillvalid;
        if (unknown.overlaps.size() == 0) return LogicStatus.CONTRADICTION;
        return result;
    }


    @Override public LogicStatus apply(Board thing) {
        Region aregion = thing.regionsById.get(regionida);
        Region bregion = thing.regionsById.get(regionidb);

        int acount = aregion.overlaps.size();
        int bcount = bregion.overlaps.size();

        if (acount > 1 && bcount > 1) return LogicStatus.STYMIED;
        if (acount == 0 || bcount == 0) return LogicStatus.CONTRADICTION;
        if (acount == 1 && bcount == 1) {
            Region.OverlappingOmino aoo = aregion.overlaps.get(0);
            Region.OverlappingOmino boo = bregion.overlaps.get(0);
            if (aoo.omino.mirrorfamily == boo.omino.mirrorfamily && adjacent(aoo,boo)) {
                return LogicStatus.CONTRADICTION;
            } else {
                return LogicStatus.STYMIED;
            }
        }
        if (acount == 1) {
            return oneToMany(aregion.overlaps.get(0),bregion);
        }
        if (bcount == 1) {
            return oneToMany(bregion.overlaps.get(0),aregion);
        }
        throw new RuntimeException("Should not get here");
    }
}
