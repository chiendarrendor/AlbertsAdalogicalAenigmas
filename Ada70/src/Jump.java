import grid.puzzlebits.Direction;

import java.awt.Point;
import java.util.Set;

public class Jump {
    Point base;
    Point terminal;
    Set<Point> intermediates;
    Direction d;


    public Jump(Point basep, Set<Point> intermediates, Point terminal,Direction d ) {
        this.base = basep;
        this.intermediates = intermediates;
        this.terminal = terminal;
        this.d = d;
    }

    public boolean isStatic() { return terminal == null; }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        if (isStatic()) {
            sb.append(base).append(" ><");
            return sb.toString();
        } else {
            sb.append(base).append(" -> (");
            boolean first = true;
            for (Point p : intermediates) {
                if (!first) sb.append(',');
                first = false;
                sb.append(p.toString());
            }

            sb.append(" ) -> " + terminal);
        }

        return sb.toString();
    }

    public boolean isLegal(Board b) {
        if (isStatic()) return b.getCell(base.x,base.y).canRemain();

        if (!b.getCell(terminal.x,terminal.y).canLand()) return false;
        for (Point p : intermediates) {
            if (!b.getCell(p.x,p.y).canPass()) return false;
        }
        return true;
    }

    public void place(Board b) {
        if (isStatic()) {
            b.setCell(base.x,base.y,PathType.TERMINAL);
            return;
        }
        PathType itype = (d==Direction.NORTH || d == Direction.SOUTH) ? PathType.VERTICAL : PathType.HORIZONTAL;
        b.setCell(base.x,base.y,PathType.INITIALEMPTY);
        b.setCell(terminal.x,terminal.y,PathType.TERMINAL);
        for (Point p : intermediates) {
            b.setCell(p.x,p.y,itype);
        }
    }



}
