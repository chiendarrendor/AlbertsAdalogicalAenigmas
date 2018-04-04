import grid.puzzlebits.Direction;

import java.util.HashSet;
import java.util.Set;

/**
 * This is the core logic element of this puzzle.
 * Every internal edge can be either unknown, one of two
 * directions, or a wall (neither direction)
 */

public class EdgeInfo {
    private Set<Direction> directions = new HashSet<>();
    boolean used = false;

    public EdgeInfo() {
    }

    public EdgeInfo(Direction initial) {
        directions.add(initial);
        directions.add(initial.getOpp());
    }

    public EdgeInfo(EdgeInfo right) {
        directions.addAll(right.directions);
        used = right.used;
    }

    public boolean canGo(Direction d) { return directions.contains(d); }
    public boolean unknown() { return directions.size() == 2; }
    public boolean isPath() { return directions.size() == 1; }
    public Direction pathDir() {
        if (!isPath()) throw new RuntimeException("Can't call pathDir on non-specific path edge!");
        return directions.iterator().next();
    }
    public boolean isWall() { return directions.size() == 0; }
    public void removeDirection(Direction d) {
        directions.remove(d);
        if (directions.size() == 0 && isUsed()) throw new RuntimeException("Can't remove last direction of a used!");
    }

    /**
     * given direction is direction to edge from operant cell, so the inbound direction opposes.
     * @param d
     */
    public void removeInbound(Direction d) {
        removeDirection(d.getOpp());
    }

    /**
     * the given direction is the direction to edge from operant cell, so outbound direction is same direction.
     * @param d
     */
    public void removeOutbound(Direction d) {
        removeDirection(d);

    }



    public void clear() {
        if (isUsed()) throw new RuntimeException("Can't clear a used!");
        directions.clear();
    }
    public void douse() {
        if (isWall()) throw new RuntimeException("Can't make a wall a used edge!");
        used = true;
    }
    public boolean isUsed() { return used; }

    /**
     * looking from cell outward in this direction to this edge, what is status?
     * @param d
     * @return
     */
    public EdgeSynopsis getSynopsis(Direction d) {
        if (isWall()) return EdgeSynopsis.WALL;
        if (unknown()) {
            return isUsed() ? EdgeSynopsis.USED_UNKNOWN : EdgeSynopsis.POSSIBLE_UNKNOWN;
        }
        // if we get here, we know we have exactly one direction.
        Direction ourd = pathDir();
        if (isUsed()) {
            if (d == ourd) return EdgeSynopsis.USED_OUT;
            return EdgeSynopsis.USED_IN;
        } else {
            if (d == ourd) return EdgeSynopsis.POSSIBLE_OUT;
            return EdgeSynopsis.POSSIBLE_IN;
        }
    }
}
