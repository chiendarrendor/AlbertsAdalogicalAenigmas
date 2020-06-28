import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PathMaker {
    Board b;
    CellContainer<Integer> distance;
    List<PathSegment> path = new ArrayList<>();
    Set<Point> onPath = new HashSet<>();

    // this represents a non-turning subset of the path between start and end.
    // cells[0] will always contain the initial point of this subset
    // (which means the previous segment's list of cells's last item will be the same celll)
    private class PathSegment {
        Direction d;
        List<Point> cells = new ArrayList<>();
        Point blocker = null; // the cell blocking further progress.
                    // if this cell is on board, it is a tree.
                    // if this cell is not on board, that means we were blocked by the edge of the board
                    // if this cell is null, that means that we had to turn before blocking (due to cost increasing)
        int terminalcost; // the cost of the final point in cells

        public Point finalPoint() { return cells.get(cells.size()-1); }
        public int getSize() { return cells.size(); }

        // assume that ix,iy is on a path (implying on the board)
        public PathSegment(int ix,int iy,Direction d,boolean nogo) {
            cells.add(new Point(ix,iy));
            this.d = d;
            terminalcost = distance.getCell(ix,iy);

            for (int i = 1 ; ; ++i) {
                Point np = d.delta(ix,iy,i);
                if (!b.inBounds(np) || b.isTree(np.x,np.y)) {
                    blocker = np;
                    break;
                }
                if (distance.getCell(np.x,np.y) > terminalcost) {
                    blocker = null;
                    break;
                }

                cells.add(np);
                terminalcost = distance.getCell(np.x,np.y);
                if (nogo) break;
            }
        }
        public PathSegment(int ix,int iy,Direction d) {
            this(ix,iy,d,false);
        }

    }




    public PathMaker(Board b) {
        this.b = b;
        distance = new CellContainer<Integer>(b.getWidth(),b.getHeight(),(x,y)->-1);

        List<Point> queue = new ArrayList<>();
        boolean foundstart = false;

        if (b.getEnd() == null) throw new RuntimeException("Can't find an end");
        if (b.getStart() == null) throw new RuntimeException("Can't find a start");

        queue.add(b.getEnd());
        distance.setCell(b.getEnd().x,b.getEnd().y,0);

        while(queue.size() > 0) {
            Point curp = queue.remove(0);
            for (Direction d : Direction.orthogonals()) {
                Point np = d.delta(curp,1);
                if (!b.inBounds(np)) continue;
                if (!b.isTile(np.x,np.y)) continue;
                if (distance.getCell(np.x,np.y) >= 0) continue;
                distance.setCell(np.x,np.y,distance.getCell(curp.x,curp.y)+1);
                queue.add(np);
            }
        }

        String dirstring = b.gfr.getVar("START");
        Direction startdir = Direction.fromShort(dirstring);

        Point curpos = b.getStart();

        path.add(new PathSegment(curpos.x,curpos.y,startdir,true));

        while(true) {
            PathSegment bestsegment = null;
            int smallest = Integer.MAX_VALUE;

            for (Direction d : Direction.orthogonals()) {
                PathSegment cursegment = new PathSegment(curpos.x,curpos.y,d);

                if (cursegment.getSize() < 2) continue;
                if (cursegment.terminalcost < smallest) { bestsegment = cursegment; smallest = cursegment.terminalcost; }
            }
            if (bestsegment == null) throw new RuntimeException("Can't find best segment");
            if (path.size() > 0 && smallest >= path.get(path.size()-1).terminalcost) throw new RuntimeException("Best segement isn't downhill!");

            curpos = bestsegment.finalPoint();
            path.add(bestsegment);
            onPath.addAll(bestsegment.cells);

            if (curpos.equals( b.getEnd())) break;
        }
    }


    public int getDistance(int x,int y) { return distance.getCell(x,y); }
    public boolean onPath(int x,int y) { return onPath.contains(new Point(x,y)); }
    public String getPathString() {
        StringBuffer sb = new StringBuffer();
        Set<Point> seenbefore = new HashSet<>();
        for (PathSegment ps : path) {
            if (ps.blocker == null) continue;
            if (!b.inBounds(ps.blocker)) continue;
            if (seenbefore.contains(ps.blocker)) continue;
            seenbefore.add(ps.blocker);
            sb.append(b.getLetter(ps.blocker.x,ps.blocker.y));
        }
        return sb.toString();
    }
}
