import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;
import grid.solverrecipes.singleloopflatten.EdgeState;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class CellPathLogicStep implements LogicStep<Board> {
    int x;
    int y;

    public CellPathLogicStep(int x, int y) { this.x = x; this.y = y; }
    public void set(int x,int y) { this.x = x; this.y = y; }

    Set<Direction> walls = new HashSet<>();
    Set<Direction> paths = new HashSet<>();
    Set<Direction> unknowns = new HashSet<>();
    private void count(Board thing) {
        walls.clear();
        paths.clear();
        unknowns.clear();
        for (Direction d : Direction.orthogonals()) {
            switch(thing.getEdge(x,y,d)) {
                case PATH: paths.add(d); break;
                case WALL: walls.add(d); break;
                case UNKNOWN: unknowns.add(d); break;
            }
        }
    }




    @Override public LogicStatus apply(Board thing) {
        count(thing);

        switch(thing.getCellType(x,y)) {
            case TERMINAL: return applyTerminal(thing);
            case STRAIGHT: return applyStraight(thing);
            case BEND: return applyBend(thing);
            case UNKNOWN: return applyUnknown(thing);
            case NOTBEND: return applyNotBend(thing);
            case NOTTERMINAL: return applyNotTerminal(thing);
            default: throw new RuntimeException("How did we get here?");
        }
    }





    private void applyTo(Board thing, Set<Direction> edges,EdgeState es) { edges.stream().forEach(e->thing.setEdge(x,y,e,es));  }

    private LogicStatus checkCount(Board thing,int minpathcount,int maxpathcount) {
        if (paths.size() > maxpathcount) return LogicStatus.CONTRADICTION;
        if (paths.size() + unknowns.size() < minpathcount) return LogicStatus.CONTRADICTION;
        if (unknowns.size() == 0) return LogicStatus.STYMIED;
        if (paths.size() == maxpathcount) {
            applyTo(thing,unknowns,EdgeState.WALL);
            return LogicStatus.LOGICED;
        }
        if (paths.size() + unknowns.size() == minpathcount) {
            applyTo(thing,unknowns,EdgeState.PATH);
            return LogicStatus.LOGICED;
        }
        return LogicStatus.STYMIED;
    }

    private LogicStatus crossCompare(Board thing, EdgeState comparetopath, EdgeState comparetowall) {
        LogicStatus result = LogicStatus.STYMIED;
        for (Direction d : Direction.orthogonals()) {
            EdgeState myedge = thing.getEdge(x,y,d);
            EdgeState oppedge = thing.getEdge(x,y,d.getOpp());
            if (myedge == EdgeState.UNKNOWN) continue;
            if (myedge == EdgeState.PATH) {
                if (oppedge == EdgeState.UNKNOWN) {
                    thing.setEdge(x,y,d.getOpp(),comparetopath);
                    result = LogicStatus.LOGICED;
                } else if (oppedge != comparetopath) {
                    return LogicStatus.CONTRADICTION;
                }
            } else {
                if (oppedge == EdgeState.UNKNOWN) {
                    thing.setEdge(x,y,d.getOpp(),comparetowall);
                    result = LogicStatus.LOGICED;
                } else if (oppedge != comparetowall) {
                    return LogicStatus.CONTRADICTION;
                }
            }
        }
        return result;
    }






    // truth table for TERMINAL cell type (exactly one output
    // # is # of unknowns
    // X = Impossible
    // C = Contradiction (too many paths)
    // W = Contradiction (too many walls)
    // ! = Stymied (3 walls, 1 path)
    // w = Logiced (all unknowns become walls)
    // p = Logiced (all unknowns become paths)
    // ? = Stymied


    // paths\walls  0   1   2   3   4
    //           0 ?4  ?3  ?2  p1  W0
    //           1 w3  w2  w1  !0   X
    //           2 C2  C1  C0   X   X
    //           3 C1  C0   X   X   X
    //           4 C0   X   X   X   X
    private LogicStatus applyTerminal(Board thing) {
        return checkCount(thing,1,1);
    }

    private LogicStatus applyStraight(Board thing)
    {
        LogicStatus result = LogicStatus.STYMIED;
        LogicStatus item = checkCount(thing,2,2);
        if (item == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
        if (item == LogicStatus.LOGICED) result = LogicStatus.LOGICED;

        item = crossCompare(thing,EdgeState.PATH,EdgeState.WALL);
        if (item == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
        if (item == LogicStatus.LOGICED) result = LogicStatus.LOGICED;

        return result;
    }



    private LogicStatus applyBend(Board thing) {
        LogicStatus result = LogicStatus.STYMIED;
        LogicStatus item = checkCount(thing,2,2);
        if (item == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
        if (item == LogicStatus.LOGICED) result = LogicStatus.LOGICED;

        item = crossCompare(thing,EdgeState.WALL,EdgeState.PATH);
        if (item == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
        if (item == LogicStatus.LOGICED) result = LogicStatus.LOGICED;

        return result;
    }

    private LogicStatus applyNotTerminal(Board thing) {
        LogicStatus result = LogicStatus.STYMIED;
        LogicStatus item = checkCount(thing,2,2);
        if (item == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
        if (item == LogicStatus.LOGICED) result = LogicStatus.LOGICED;

        return result;
    }

    private LogicStatus applyUnknown(Board thing) {
        LogicStatus result = LogicStatus.STYMIED;
        LogicStatus item = checkCount(thing,1,2);
        if (item == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
        if (item == LogicStatus.LOGICED) result = LogicStatus.LOGICED;

        return result;
    }

    private LogicStatus applyNotBend(Board thing) {
        LogicStatus result = LogicStatus.STYMIED;
        LogicStatus item = checkCount(thing,1,2);
        if (item == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
        if (item == LogicStatus.LOGICED) result = LogicStatus.LOGICED;



        return result;
    }





}

