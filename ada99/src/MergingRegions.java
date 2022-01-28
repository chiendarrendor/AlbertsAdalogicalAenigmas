import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MergingRegions {
    private class Region {
        int id;
        Set<Point> contents = new HashSet<>();

        public Region(int x,int y) {
            id = 10000*x+y;
            contents.add(new Point(x,y));
        }

        // this destructively merges other into this
        public void merge(Region other) {
            for (Point p : other.contents) {
                regions.setCell(p.x,p.y,this);
                contents.add(p);
            }
            regionsById.remove(other.id);
        }
    }

    CellContainer<Region> regions;
    Map<Integer,Region> regionsById = new HashMap<>();

    public MergingRegions(int width,int height,String[][] linkers) {
        regions = new CellContainer<Region>(width,height,
                (x,y)->{
                    Region r = new Region(x,y);
                    regionsById.put(r.id,r);
                    return r;
                },
                (x,y,r)->{throw new RuntimeException("Wasn't designed to be copied!");}
        );
        for (int y = 0 ; y < height; ++y) {
            for (int x = 0 ; x < width ; ++x) {
                if (".".equals(linkers[x][y])) continue;
                Direction d = Direction.fromShort(linkers[x][y]);
                Point op = d.delta(x,y,1);
                regions.getCell(x,y).merge(regions.getCell(op.x,op.y));
            }
        }
    }

    public Collection<Integer> getRegionIds() { return regionsById.keySet(); }
    public Set<Point> getRegionPoints(int rid) { return regionsById.get(rid).contents; }
    public int getRegionId(int x,int y) { return regions.getCell(x,y).id; }

}
