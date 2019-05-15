import grid.logic.LogicStatus;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AdjacentRegionLogicStep implements grid.logic.LogicStep<Board> {
    private static class RegionInfo {
        char id;
        int basei;
        Set<Point> cells = new HashSet<>();

        public void addTP(TemplatePointer tp) {
            char myid = tp.getTemplate().id;
            int mybase = tp.getTemplate().idx / 3 * 3;
            Point curp = tp.getBasePoint();

            if (cells.size() == 0) {
                id = myid;
                basei = mybase;
            } else if (cells.size() == 3) {
                throw new RuntimeException("Region shouldn't have more than 3 cells!");
            } else if (myid != id) {
                throw new RuntimeException("Cells of region should all have the same shape!");
            } else if (basei != mybase) {
                throw new RuntimeException("Cells of region should all have the same base region index!");
            }

            cells.add(curp);
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("Region Id: ").append(id);
            sb.append(" Region Id Base Index: ").append(basei);
            sb.append(" (");
            for (Point p : cells) sb.append(p);
            sb.append(")");
            return sb.toString();
        }


    }

    private static void gocRegion(Map<Integer,RegionInfo> themap,TemplatePointer tp) {
        int regionid = tp.getRegionId();
        if (!themap.containsKey(regionid)) {
            themap.put(regionid,new RegionInfo());
        }
        themap.get(regionid).addTP(tp);
    }



    @Override public LogicStatus apply(Board thing) {
        Map<Integer,RegionInfo> regions = new HashMap<>();
        thing.forEachCell((x,y)->{
            TemplatePointer tp = thing.getCell(x,y);
            if (tp == null) return;
            gocRegion(regions,tp);
        });

        LogicStatus result = LogicStatus.STYMIED;
        for (RegionInfo region : regions.values()) {
            Set<Point> adjacents = new HashSet<>();
            for (Point p : region.cells) {
                for (Direction d : Direction.orthogonals()) {
                    Point np = d.delta(p,1);
                    if (region.cells.contains(np)) continue;
                    if (!thing.onBoard(np.x,np.y)) continue;
                    if (thing.isBlock(np.x,np.y)) continue;
                    adjacents.add(np);
                }
            }

            for (Point p : adjacents) {
                TemplatePointer tp = thing.getCell(p.x,p.y);

                if (tp != null) {
                    if (tp.getTemplate().id == region.id) return LogicStatus.CONTRADICTION;
                } else {
                    for (int i = 0 ; i < 3 ; ++i ) {
                        int tridx = region.basei + i;
                        if (thing.isPossible(p.x,p.y,tridx)) {
                            thing.makeImpossible(p.x,p.y,tridx);
                            result = LogicStatus.LOGICED;
                        }
                    }
                }
            }
        }
        return result;
    }
}
