
import grid.puzzlebits.Direction;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class Circle {
    Set<Path> paths = new HashSet<Path>();
    boolean locked = false; // true if the one remaining path has been processed onto the board
    Board b;

    // dist will be 0 or more, or -1, meaning no particular distance
    public Circle(int x,int y,Board b,int dist) {
        this.b = b;
        Path base = new Path(new Point(x,y),dist);
        if (dist == -1 || dist == 0) addPath(base);

        for (Direction d : Direction.orthogonals()) {
            Path dircur = base;
            for(int delta = 1;;++delta) {
                Point np = new Point(x+d.DX()*delta,y+d.DY()*delta);

                if (!b.onBoard(np)) break;
                if (dist != -1 && delta > dist) break;
                if (b.getCellState(np.x,np.y) != CellState.EMPTY) break;

                dircur = new Path(dircur);
                dircur.addPoint(np);
                if (dist == -1 || delta == dist) addPath(dircur);
            }
        }

    }

    public Circle(Circle right,Board b)
    {
        this.b = b;
        paths.addAll(right.paths);
        locked = right.locked;
    }

    private void addPath(Path p) {
        paths.add(p);
        b.getPossiblePaths(p.terminal.x,p.terminal.y).paths.add(p);
    }

    private void removePath(Path p) {
        paths.remove(p);
        b.getPossiblePaths(p.terminal.x,p.terminal.y).paths.remove(p);
    }

    // this is only used when we know the Path of the cell
    private void setCellState(Point p,Path path,CellState cs) {
        b.setCellState(p.x,p.y,cs);
        b.setSetPath(p.x,p.y,path);
    }



    public boolean hasPath(Path p) { return paths.contains(p); }
    public void validatePath(Path p) { if (!hasPath(p)) throw new RuntimeException("I don't own this path!"); }


    public void removeOnePath(Path p) { validatePath(p); removePath(p);}
    public void removeAllPathsBesides(Path p) { validatePath(p); Stream.of(paths.toArray(new Path[0])).forEach((pth)->removePath(pth));addPath(p); }
    public boolean isLocked() { return locked; }
    public boolean isLockable() { return paths.size() == 1 && placeable(paths.iterator().next()); }

    // caller may only call lock when there is a single placeable path
    public void lock() {
        if (!isLockable()) throw new RuntimeException("Can't lock this!");
        locked = true;

        Path thePath = paths.iterator().next();

        setCellState(thePath.initial,thePath,CellState.PATH);
        thePath.pathPoints.stream().forEach((pt)->setCellState(pt,thePath,CellState.PATH));
        setCellState(thePath.terminal,thePath,CellState.TERMINAL);
    }

    public boolean placeable(Path p) {
        if (!hasPath(p)) throw new RuntimeException("Why asking placeable when I don't own this path?");
        return p.pathPoints.stream().allMatch((pt)->b.getCellState(pt.x,pt.y)==CellState.EMPTY);
    }




    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("(").append(paths.size()).append(")");
        paths.stream().forEach((x)-> sb.append(x.toString()).append("\n"));
        return sb.toString();
    }
}
