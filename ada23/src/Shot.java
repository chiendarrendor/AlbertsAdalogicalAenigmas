import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;

import java.util.ArrayList;
import java.util.List;
import java.awt.Point;

public class Shot extends ShotHolder
{
    Direction d;
    int length;
    Point starting;
    Point ending;
    Shot parent;
    boolean isValid;
    boolean holed;
    // this will be true if we are invalid because we hit something (edge of the board, another path, etc)
    // equivalent:  if blocked is true, a longer shot from our starting would also be blocked
    boolean blocked;

    public Shot(Shot parent,Direction d,int length,Point starting,Board b) {
        this.parent = parent;
        this.d = d;
        this.length = length;
        this.starting = starting;
        this.ending = d.delta(starting,length);
        isValid = validate(b);
        if (!isValid) return;
        if (holed) return;
        // if we get here, we're ourselves valid, but we're not holed.
        if (length == 1) {
            isValid = false;
            return;
        }
        // if we get here, we're valid, we're not holed, but we still can take another shot!
        // our validity is based on whether or not we have children!
        isValid = takeInitialShot(length-1,ending,b,this);
    }

    public Shot(Shot right) {
        super(right);
        this.parent = null;
        this.d = right.d;
        this.length = right.length;
        this.starting = right.starting;
        this.ending = right.ending;
        this.isValid = right.isValid;
        this.holed = right.holed;
        this.blocked = right.blocked;

    }

    private boolean validate(Board b)
    {
        blocked = false;
        if (!b.onBoard(ending)) {
            blocked = true;
            return false;
        }
        switch(b.getCellStates().getCell(ending.x,ending.y)) {
            case HOLE:
                holed = true;
                break;

            case BALL:
            case PATH:
            case LIE:
            case FULL:
                blocked = true;
                return false;

            case TRAP:
                return false;

            case EMPTY:
                break;

        }

        for (int i = 1 ; i < length ; ++i) {
            Point p = d.delta(starting,i);
            switch(b.getCellStates().getCell(p.x,p.y)) {
                case BALL:
                case PATH:
                case LIE:
                case FULL:
                case HOLE:
                    blocked = true;
                    return false;
                case EMPTY:
                case TRAP:
                    break;
            }
        }

        if (parent != null) {
            for (int i = 1; i <= length; ++i) {
                Point p = d.delta(starting, i);
                if (parent.pathCovers(p)) {
                    blocked = true;
                    return false;
                }
            }
        }

        return true;
    }

    private boolean pathCovers(Point p) {
        if (parent != null && parent.pathCovers(p)) return true;
        switch(d) {
            case NORTH:
                return p.x == starting.x && ending.y <= p.y && p.y <= starting.y;
            case SOUTH:
                return p.x == starting.x && starting.y <= p.y && p.y <= ending.y;
            case EAST:
                return p.y == starting.y && starting.x <= p.x && p.x <= ending.x;
            case WEST:
                return p.y == starting.y && ending.x <= p.x && p.x <= starting.x;
            default: throw new RuntimeException("WTF?");
        }
    }

    public boolean isValid() { return isValid; }
    public boolean holed() { return holed; }
    public boolean isBlocked() { return blocked; }
    public void setParent(Shot parent) {
        this.parent = parent;
        super.setParent(this);
    }

    public void show(String indent) {
        System.out.format("%s%d,%d -> %s-%d -> %s%d,%d%s (%d)%n",
                indent,
                starting.x,starting.y,
                d,length,
                (holed() ? "(" : ""),
                ending.x,ending.y,
                (holed() ? ")" : ""),
                count()
                );

        final String thisString = this.toString();
        stream().forEach((x)-> x.show(indent + "  ") );
    }

    public int count() {
        return super.count() + (holed() ? 1 : 0);
    }

    public List<ShotName> getShotNames(ShotName name) {
        List<ShotName> result = new ArrayList<>();
        ShotName myname = new ShotName(name,d);
        if (holed()) result.add(myname);
        result.addAll(super.getShotNames(myname));
        return result;
    }

    public void glueDown(Board b) {
        for (int i = 1; i < length ; ++i) {
            Point np = d.delta(starting,i);
            b.getCellStates().setCell(np.x,np.y,CellState.PATH);
        }
        b.getCellStates().setCell(ending.x,ending.y,holed() ? CellState.FULL : CellState.LIE);
        super.glueDown(b);
    }

    public boolean revalidate(Board b) {
        for (int i = 1 ; i <= length ; ++i) {
            Point np = d.delta(starting,i);
            switch(b.getCellStates().getCell(np.x,np.y)) {
                case PATH:
                case LIE:
                case FULL:
                    return false;
            }
        }
        if (holed()) return true;

        return super.revalidate(b);
    }

    public void fillShotInfo(CellContainer<Shot> shinfo) {
        for (int i = 0 ; i <= length ; ++i) {
            Point np = d.delta(starting,i);
            shinfo.setCell(np.x,np.y,this);
        }
        super.fillShotInfo(shinfo);
    }
}
