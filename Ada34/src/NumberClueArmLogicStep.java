import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;
import grid.puzzlebits.Path.GridPathCell;
import grid.puzzlebits.Path.Path;
import grid.solverrecipes.singleloopflatten.EdgeState;

import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class NumberClueArmLogicStep implements LogicStep<Board> {
    int x;
    int y;
    int size;
    public NumberClueArmLogicStep(int x,int y,int size) { this.x = x; this.y = y; this.size = size; }

    private boolean onDifferentBentPath(Board b, int delta, Direction d) {
        Point curp = d.delta(x,y,delta);
        GridPathCell gpc = b.getPathContainer().getCell(curp.x,curp.y);
        Path path = null;
        if (gpc.getTerminalPaths().size() > 0) path = gpc.getTerminalPaths().get(0);
        if (gpc.getInternalPaths().size() > 0) path = gpc.getInternalPaths().get(0);

        if (path == null) return false;

        boolean hasbend = false;
        boolean isus = false;

        for (Point p : path) {
            if (p.x == x && p.y == y) isus = true;
            if (b.getCellType(p.x,p.y) == CellType.BEND) hasbend = true;
        }

        return hasbend && !isus;
    }


    private boolean canLeave(Board b,int delta,Direction d) {
        Point curp = d.delta(x,y,delta);
        return b.getEdge(curp.x,curp.y,d) != EdgeState.WALL;
    }

    private boolean canEnter(Board b, int delta, Direction d) {
        Point curp = d.delta(x,y,delta);
        if (b.getCellType(curp.x,curp.y) == CellType.BEND) return false;
        for (Direction sd: Direction.orthogonals()) {
            if (sd == d) continue;
            if (sd == d.getOpp()) continue;
            if (b.getEdge(curp.x,curp.y,sd) == EdgeState.PATH) return false;
        }
        if (onDifferentBentPath(b,delta,d)) return false;
        return true;
    }

    Direction[] pia = new Direction[] { Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST };
    Direction[] pib = new Direction[] { Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH };

    private Integer validateAddLengths(Map<Direction,Integer> lengths, int pairindex) {
        if (lengths.get(pia[pairindex]) + lengths.get(pib[pairindex]) + 1 < size) return null;
        return pairindex;
    }

    private boolean pairOverlaps(int p1, int p2) {
        return (p1 % 2) != (p2 % 2);
    }

    private Direction overlappingDirection(int p1,int p2) {
        int lp = Math.min(p1,p2);
        int hp = Math.max(p1,p2);
        if (lp == 0 && hp == 1) return Direction.EAST;
        if (lp == 1 && hp == 2) return Direction.SOUTH;
        if (lp == 2 && hp == 3) return Direction.WEST;
        if (lp == 0 && hp == 3) return Direction.NORTH;
        throw new RuntimeException("can't find overlapping direction for pairs " + p1 + " and " + p2);
    }

    private LogicStatus force(Board b, Direction d) {
        LogicStatus result = LogicStatus.STYMIED;
        if (b.getEdge(x,y,d) == EdgeState.WALL) return LogicStatus.CONTRADICTION;
        if (b.getEdge(x,y,d) == EdgeState.UNKNOWN) { b.setEdge(x,y,d,EdgeState.PATH); result = LogicStatus.LOGICED; }
        if (b.getEdge(x,y,d.getOpp()) == EdgeState.PATH) return LogicStatus.CONTRADICTION;
        if (b.getEdge(x,y,d.getOpp()) == EdgeState.UNKNOWN) { b.setEdge(x,y,d.getOpp(),EdgeState.WALL); result = LogicStatus.LOGICED; }
        return result;
    }

    private LogicStatus extend(Board b, Direction d, int count) {
        LogicStatus result = LogicStatus.STYMIED;
        for (int i = 1 ; i < count ; ++i) {
            Point dp = d.delta(x,y,i);
            if (b.getEdge(dp.x,dp.y,d) == EdgeState.UNKNOWN) {
                b.setEdge(dp.x,dp.y,d,EdgeState.PATH);
                result = LogicStatus.LOGICED;
            }
        }
        return result;
    }


    @Override public LogicStatus apply(Board thing) {
        if (size < 3) return LogicStatus.STYMIED;
        Map<Direction,Integer> lengths = new HashMap<>();

        for (Direction d : Direction.orthogonals()) {
            int cursize = 0;
            for ( ; ; ++cursize) {
                if (!canLeave(thing, cursize, d)) break;
                if (!canEnter(thing, cursize + 1, d)) break;
            }
            lengths.put(d,cursize);
        }

        Set<Integer> validated = new HashSet<>();

        for(int i = 0 ; i < 4 ; ++ i) {
            validated.add(validateAddLengths(lengths,i));
        }
        validated.remove(null);

        LogicStatus result = LogicStatus.STYMIED;

        if (validated.size() > 2) return LogicStatus.STYMIED;
        if (validated.size() < 1) return LogicStatus.CONTRADICTION;

        if (validated.size() == 1) {
            int pairindex = validated.iterator().next();
            LogicStatus tls = force(thing,pia[pairindex]);
            if (tls == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
            if (tls == LogicStatus.LOGICED) result = LogicStatus.LOGICED;

            tls = force(thing,pib[pairindex]);
            if (tls == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
            if (tls == LogicStatus.LOGICED) result = LogicStatus.LOGICED;

            int aleftovers = size - 1 - lengths.get(pia[pairindex]);
            if (aleftovers > 1) {
                tls = extend(thing,pib[pairindex],aleftovers);
                if (tls == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
                if (tls == LogicStatus.LOGICED) result = LogicStatus.LOGICED;
            }
            int bleftovers = size - 1 - lengths.get(pib[pairindex]);
            if (bleftovers > 1) {
                tls = extend(thing,pia[pairindex],bleftovers);
                if (tls == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
                if (tls == LogicStatus.LOGICED) result = LogicStatus.LOGICED;
            }
        }

        if (validated.size() == 2) {
            Iterator<Integer> pit = validated.iterator();
            int p1 = pit.next();
            int p2 = pit.next();

            if (pairOverlaps(p1,p2)) {
                Direction overlapdir = overlappingDirection(p1, p2);
                LogicStatus tls = force(thing, overlapdir);
                if (tls == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
                if (tls == LogicStatus.LOGICED) result = LogicStatus.LOGICED;

                int otherarmlength = 0;
                for (Direction d : Direction.orthogonals()) {
                    if (d == overlapdir) continue;
                    if (d.getOpp() == overlapdir) continue;
                    if (lengths.get(d) > otherarmlength) otherarmlength = lengths.get(d);
                }

                int leftovers = size - 1 - otherarmlength;
                if (leftovers > 1) {
                    tls = extend(thing,overlapdir,leftovers);
                    if (tls == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
                    if (tls == LogicStatus.LOGICED) result = LogicStatus.LOGICED;
                }

            }



        }



/*
        System.out.println("Direction Lengths: ");
        for (Direction d : Direction.orthogonals()) {
            System.out.println(d + ": " + lengths.get(d));
        }

        System.out.println("Validated Pairs: ");
        for (int idx : validated) { System.out.println(idx); }
*/


        return result;
    }
}
