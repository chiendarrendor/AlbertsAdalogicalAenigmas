import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegionGraph {
    int width;
    int height;
    Map<Integer,Region> regions = new HashMap<>();
    Map<Point,Region> byPoints = new HashMap<>();
    List<Edge> edges = new ArrayList<>();

    public RegionGraph(int width,int height) {this.width = width; this.height = height; }
    public RegionGraph(RegionGraph right) {
        width = right.width;
        height = right.height;

        for (int i : right.regions.keySet() ) {
            Region newregion = new Region(right.regions.get(i));
            for (Point p : newregion.cells) {
                byPoints.put(p,newregion);
            }
            regions.put(i,newregion);
        }

        for (Edge e : right.edges) {
            Edge newedge = new Edge(e);

            int r1 = newedge.regionid1;
            int r2 = newedge.regionid2;

            regions.get(r1).addNeighbor(r2,newedge);
            regions.get(r2).addNeighbor(r1,newedge);
            edges.add(newedge);
        }
    }


    public Region cellIsRegion(int x,int y, int rid) {
        Point p = new Point(x,y);
        Region r = null;
        if (byPoints.containsKey(p)) return byPoints.get(p);

        if (!regions.containsKey(rid)) {
            r = new Region(rid);
            regions.put(rid, r);
        } else {
            r = regions.get(rid);
        }

        byPoints.put(p,r);
        r.addCell(x,y);
        return r;
    }

    // can reasonably assume that the caller has told us that this cell is a region already
    public void cellHasLetter(int x,int y, char letter, Direction letterdir) {
        Point p = new Point(x,y);
        Region r = byPoints.get(p);
        r.setLetter(x,y,letter,letterdir);
    }

    // can reasonably assume that our end is created, but not necessarily the other end.

    public void edgeIsBorder(int x,int y,Direction d,int neighborrid) {
        Point p = new Point(x,y);
        Point np = d.delta(p,1);
        Region myregion = byPoints.get(p);
        int myid = myregion.regionid;
        Region neighbor = cellIsRegion(np.x,np.y,neighborrid); // get-or-create!

        if (!myregion.edges.containsKey(neighborrid)) {
            Edge e = new Edge(myid,neighborrid);
            myregion.addNeighbor(neighborrid,e);
            neighbor.addNeighbor(myid,e);
            edges.add(e);
        }
        myregion.setEdge(x,y,d,neighborrid);
    }

    // this can only be done after all regions have been properly set up.
    public void calculatePairs() {
        for (Edge e : edges) {
            Region r1 = regions.get(e.regionid1);
            Region r2 = regions.get(e.regionid2);
            RegionPair rp = new RegionPair(r1.cells,r2.cells);
            e.setPair(rp);
        }
    }


}
