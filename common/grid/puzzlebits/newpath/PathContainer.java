package grid.puzzlebits.newpath;

import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PathContainer {
    public class Path {
        private int id;
        private List<Point> cells = new ArrayList<>();
        private boolean isLoop = false;
        private boolean isReversible;

        public Path(int id,Point first,Point last,boolean isReversible) {
            this.id = id;
            cells.add(first);
            cells.add(last);
            this.isReversible = isReversible;
        }

        public Path(Path right) {
            id = right.id;
            cells.addAll(right.cells);
            isLoop = right.isLoop;
            isReversible = right.isReversible;
        }

        public void addCell(Point p) { cells.add(p); }
        public boolean isReversible() { return isReversible; }
        public boolean isLoop() { return isLoop; }
        public void makeIrreversible() { isReversible = false; }
        public Point head() { return cells.get(0); }
        public Point tail() { return cells.get(cells.size()-1); }
        public int getId() { return id; }
        public List<Point> getCells() { return cells; }

        public void reverse() {
            if (!isReversible) throw new RuntimeException("Shouldn't reverse an irreversible path!");
            List<Point> newlist = new ArrayList<>();
            cells.stream().forEach(p->newlist.add(0,p));
            cells = newlist;
        }

        public void makeLoop() {
            if (!head().equals(tail())) throw new RuntimeException("tried to make loop of path with nonoverlapping termini");
            cells.remove(cells.size()-1);
            isLoop = true;
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("Path ").append(getId()).append("(");
            sb.append(isLoop ? "L" : "S");
            sb.append(isReversible ? "R" : "I");
            sb.append(") ");
            cells.stream().forEach(p->sb.append("(").append(p.x).append(",").append(p.y).append(") "));
            return sb.toString();
        }
    }

    public class Port {
        private Point cell;
        private Direction d; // side of cell path comes in/out on
        private Path p;
        private boolean isHead;

        public Port(Point cell,Direction d, Path p , boolean isHead) { this.cell = cell; this.d = d; this.p = p; this.isHead = isHead; }
        public Point getLocation() { return cell; }
        public Direction getDirection() { return d; }
        public Path getPath() { return p; }
        public boolean isHead() { return isHead; }
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("Port of (").append(cell.x).append(",").append(cell.y).append(")(");
            sb.append(d).append("->").append(p.getId()).append(" ").append(isHead ? "head" : "tail").append(")");
            return sb.toString();
        }
    }

    private class PathCell {
        List<Path> nonTerminalPaths = new ArrayList<>();
        Map<Direction,Port> ports = new HashMap<>();
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("  ").append("(");
            nonTerminalPaths.stream().forEach(p->sb.append(p.getId()).append(" "));
            sb.append(")").append("\n");
            ports.keySet().forEach(d->sb.append("  ").append(d).append(": ").append(ports.get(d)).append("\n"));
            return sb.toString();
        }
    }

    public interface PathContainerAssistant {
        public int getWidth();
        public int getHeight();
        public boolean isLinkable(Port porta,Port portb);
        public int maxPorts(int x,int y); // what is the maximum # of edges of this cell that can have paths?
        public int maxUnlinked(int x,int y); // what is the maximum # of ports that can remain unlinked after a link attempt?
    }

    private CellContainer<PathCell> cells;
    private PathContainerAssistant pca;
    private int nextpathid = 0;
    private List<Path> paths = new ArrayList();
    private Set<Point> dirtycells = new HashSet();

    private boolean isDirty(int x,int y) { return dirtycells.contains(new Point(x,y)); }
    private boolean isDirty(Point p) { return dirtycells.contains(p); }
    public int getWidth() { return cells.getWidth(); }
    public int getHeight() { return cells.getHeight(); }
    public List<Path> getPaths() { return paths; }


    private PathCell getOrCreatePathCell(int x,int y) {
        if (cells.getCell(x,y) == null) cells.setCell(x,y,new PathCell());
        return cells.getCell(x,y);
    }

    // we are going to assume that the PathCell and Port are available for the given Path
    private Port getPortForPath(Path path,boolean isHead) {
        if (path.isLoop()) return null;
        Point opend = isHead ? path.head() : path.tail();
        PathCell pc = cells.getCell(opend.x,opend.y);
        for (Port port : pc.ports.values()) {
            if (port.isHead() == isHead && port.p == path) return port;
        }
        System.out.println(PathContainer.this.toString());
        throw new RuntimeException("Can't find Port for Path Terminal? " + path.getId() + " " + isHead);
    }

    private void reverse(Path path) {
        Port headPort = getPortForPath(path,true);
        Port tailPort = getPortForPath(path,false);
        path.reverse();
        headPort.isHead = false;
        tailPort.isHead = true;
    }



    // this method only used during copy construct
    private void setNewPath(Path path,Path oldPath,PathContainer oldContainer) {
        for (Point point : path.cells) {
            if (!path.isLoop() && point.equals(path.head())) {
                Port oldport = oldContainer.getPortForPath(oldPath,true);
                Port newport = new Port(point,oldport.getDirection(),path,true);
                getOrCreatePathCell(point.x,point.y).ports.put(oldport.getDirection(),newport);
                continue;
            }
            if (!path.isLoop() && point.equals(path.tail())) {
                Port oldport = oldContainer.getPortForPath(oldPath,false);
                Port newport = new Port(point,oldport.getDirection(),path,false);
                getOrCreatePathCell(point.x,point.y).ports.put(oldport.getDirection(),newport);
                continue;
            }

            getOrCreatePathCell(point.x,point.y).nonTerminalPaths.add(path);
        }
    }

    public PathContainer(PathContainerAssistant pca) {
        this.pca = pca;
        cells = new CellContainer<PathCell>(pca.getWidth(),pca.getHeight(),(x,y)->null,(x,y,r)->null);
    }

    public PathContainer(PathContainer right) {
        this.pca = right.pca;
        this.nextpathid = right.nextpathid;
        cells = new CellContainer<PathCell>(pca.getWidth(),pca.getHeight(),(x,y)->null,(x,y,r)->null);
        for (Path p : right.paths) {
            Path newp = new Path(p);
            setNewPath(newp,p,right);
            paths.add(newp);
        }
        dirtycells.addAll(right.dirtycells);
    }

    // this is the unit of addition to this container...head and tail must be adjacent, according to Direction
    public void newPair(Point head,Point tail,boolean isReversible) {
        Direction d = Direction.fromTo(head.x,head.y,tail.x,tail.y);

        Path path = new Path(nextpathid++,head,tail,isReversible);
        paths.add(path);

        PathCell headcell = getOrCreatePathCell(head.x,head.y);
        Port headport = new Port(head,d,path,true);
        headcell.ports.put(d,headport);

        PathCell tailcell = getOrCreatePathCell(tail.x,tail.y);
        Port tailport = new Port(tail,d.getOpp(),path,false);
        tailcell.ports.put(d.getOpp(),tailport);

        dirtycells.add(head);
        dirtycells.add(tail);
    }

    // cases:
    // Port1 Reversible
    // Port2 Reversible
    // Port1 isHead
    // Port2 isHead
    // P1R  P2R  P1H  P2H      what to do            logic type
    //   0    0    0    0      UNLINKABLE            A
    //   0    0    1    1      UNLINKABLE            A
    //   0    1    0x   0(1)      reverse 2, 1->2       D1
    //   1    1    0    0(1)      reverse 2, 1->2       D1
    //   0    1    1x   1(0)      reverse 2, 2->1       D2
    //   1    1    1    1(0)      reverse 2, 2->1       D2
    //   1    0    1(0)    1x     reverse 1, 1->2      E1
    //   1    0    0(1)    0x     reverse 1, 2->1      E2

    //   0    0    0    1      1->2                  B
    //   0    1    0x   1      1->2                  B
    //   1    1    0    1      1->2                  B
    //   1    0    0    1x     1->2                  B

    //   0    0    1    0      2->1                  C
    //   1    0    1    0x     2->1                  C
    //   0    1    1x   0      2->1                  C
    //   1    1    1    0      2->1                  C




    private boolean cleanOneCell(Point cellp) {
        PathCell cell = cells.getCell(cellp.x,cellp.y);
        if (2 * cell.nonTerminalPaths.size() + cell.ports.size() > pca.maxPorts(cellp.x,cellp.y)) {
            return false;
        }

        if (cell.ports.size() < 2) return true;

        Direction[] keys = cell.ports.keySet().toArray(new Direction[0]);

        Set<Direction> handled = new HashSet<>();
        for (int i1 = 0 ; i1 < keys.length ; ++i1) {
            Direction d1 = keys[i1];
            if (handled.contains(d1)) continue;
            Port port1 = cell.ports.get(d1);
            for (int i2 = i1 + 1 ; i2 < keys.length ; ++i2 ) {
                Direction d2 = keys[i2];
                if (handled.contains(d2)) continue;

                Port port2 = cell.ports.get(d2);

//                System.out.println("Cell " + cellp + "p1: " + port1 + " p2: " + port2);

                if (!pca.isLinkable(port1,port2)) continue;

                // if we get here, we can link.
                Port fromPort = null;
                Port toPort = null;

                // if the two ports are head-to-head or tail-to-tail, we have to reverse one.
                if (port1.isHead() == port2.isHead()) {
                    // if we can't reverse one of them, then we can't do this linking.
                    if (!port1.getPath().isReversible() && !port2.getPath().isReversible()) return false;

                    if (port2.getPath().isReversible()) {
                        reverse(port2.getPath());
                    } else {
                        reverse(port1.getPath());
                    }
                }

                // if we get here, we should the two paths head-to-tail...figure out which direction it goes.
                if (!port1.isHead() && port2.isHead()) {
                    fromPort = port1;  toPort = port2;
                } else {
                    fromPort = port2; toPort = port1;
                }

                // now that we're here, the tail end of fromPath, going into fromPort, should be linkable to
                // the head end of toPath, going into toPort.  Specifically, we are going to do away with
                // toPath, and extend fromPath out

                cell.ports.remove(fromPort.getDirection());
                cell.ports.remove(toPort.getDirection());
                handled.add(fromPort.getDirection());
                handled.add(toPort.getDirection());
                cell.nonTerminalPaths.add(fromPort.getPath());
                if(!toPort.getPath().isReversible()) fromPort.getPath().makeIrreversible();

                if (fromPort.getPath() == toPort.getPath()) {
                    fromPort.getPath().makeLoop();
                } else {
                    // ignore the first cell of toPath...it's this cell or we wouldn't be here.
                    for (int i = 1 ; i < toPort.getPath().cells.size() ; ++i) {
                        Point curto = toPort.getPath().cells.get(i);
                        fromPort.getPath().cells.add(curto);

                        if (i == toPort.getPath().cells.size() - 1) {
                            getPortForPath(toPort.getPath(),false).p = fromPort.getPath();
                        } else {
                            PathCell curtopc = cells.getCell(curto.x,curto.y);
                            curtopc.nonTerminalPaths.remove(toPort.getPath());
                            curtopc.nonTerminalPaths.add(fromPort.getPath());
                        }
                    }
                    paths.remove(toPort.getPath());
                }

                // if we get here, we  successfully processed the path1/path2 pair, and removed the corresponding ports
                // so we shouldn't try to use path1 again.
                break;
            }
        }

        // check to make sure we did the minimum required work.
        if (cell.ports.size() > pca.maxUnlinked(cellp.x,cellp.y)) return false;
        return true;
    }




    public boolean clean() {
        for (Point curpoint : dirtycells) {
            if (!cleanOneCell(curpoint)) return false;
        }

        dirtycells.clear();
        return true;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();

        for(Path p: paths) {
            sb.append(p.toString()).append("\n");
        }
        for(int y = 0 ; y < getHeight() ; ++y) {
            for (int x = 0 ; x < getWidth() ; ++x ) {
                if (cells.getCell(x,y) == null) continue;
                sb.append("(").append(x).append(",").append(y).append(")\n");
                sb.append(cells.getCell(x,y).toString());
            }
        }
        return sb.toString();
    }


}
