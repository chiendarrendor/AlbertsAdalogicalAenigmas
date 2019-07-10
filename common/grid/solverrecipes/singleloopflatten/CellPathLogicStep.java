package grid.solverrecipes.singleloopflatten;

import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.logic.flatten.FlattenLogicer;
import grid.puzzlebits.Direction;

import java.util.Vector;

public class CellPathLogicStep<T extends SingleLoopBoard<T>> implements LogicStep<T> {
    int x;
    int y;
    boolean cellMustHavePath;

    public static <Q extends SingleLoopBoard<Q>> void generateLogicSteps(FlattenLogicer<Q> solver,Q master,boolean cellMustHavePath) {
        master.forEachCell((x,y)->solver.addLogicStep(new CellPathLogicStep<>(x,y,cellMustHavePath)));
    }



    public CellPathLogicStep(int x,int y,boolean cellMustHavePath) {this.x = x; this.y = y; this.cellMustHavePath = cellMustHavePath; }

    Vector<Direction> unknowns = new Vector<>();
    int pathcount;
    int wallcount;

    private void processEdges(SingleLoopBoard thing)
    {
        pathcount = 0;
        wallcount = 0;
        unknowns.clear();

        for (Direction d : Direction.orthogonals())
        {
            switch (thing.getEdge(x,y,d))
            {
                case PATH: ++pathcount; break;
                case WALL: ++wallcount; break;
                case UNKNOWN: unknowns.add(d); break;
            }
        }
    }

    private void setUnknowns(SingleLoopBoard thing, EdgeState et) { for(Direction d : unknowns) thing.setEdge(x,y,d,et); }

    // X = impossible (we're counting 4 things)
    // # = # of unknowns
    // ! = pathcount > 2
    // @ = pathcount = 1 && wallcount = 3
    // ? = pathcount = 0 && wallcount = 4 (ok only if we allow cells to not have paths
    // * = pathcount = 2 && wallcount = 2 (always okay)
    // W = 2 paths, all remaining edges must be walls
    // P = 1 path, 1 way to go...go that way
    // Q = 1 path, multiple ways to go...can't do anything
    // V = 3 walls...a 4th wall, only if we allow cells to not have paths
    // W = 2 walls ... a path if cell must have path, otherwise we don't know
    // - = 1 or no walls, no paths...we have no idea what's going on with this cell.

    //                      Pathcount
    // Wallcount        0       1       2       3       4
    //          0       4-      3Q      2W      1!      0!
    //          1       3-      2Q      1W      0!      X
    //          2       2W      1P      0*      X       X
    //          3       1V      0@      X       X       X
    //          4       0?      X       X       X       X



    @Override public LogicStatus apply(SingleLoopBoard thing) {
        processEdges(thing);

        if (pathcount > 2) return LogicStatus.CONTRADICTION;
        if (pathcount == 1 && wallcount == 3) return LogicStatus.CONTRADICTION;
        if (pathcount == 2 && wallcount == 2) return LogicStatus.STYMIED;
        if (pathcount == 0 && wallcount == 4) {
            return cellMustHavePath ? LogicStatus.CONTRADICTION : LogicStatus.STYMIED;
        }

        // by logic and the chart above, if we get here, we have unknowns.
        if (pathcount == 2) {
            setUnknowns(thing,EdgeState.WALL);
            return LogicStatus.LOGICED;
        }

        if (pathcount == 1) {
            if (unknowns.size() == 1) {
                setUnknowns(thing,EdgeState.PATH);
                return LogicStatus.LOGICED;
            }
            // if we have one path, but more than one unknown, we don't know which way to go.
            return LogicStatus.STYMIED;
        }

        // we only get here if pathcount is 0
        if (wallcount == 3) {
            if (cellMustHavePath) return LogicStatus.CONTRADICTION;
            setUnknowns(thing,EdgeState.WALL);
            return LogicStatus.LOGICED;
        }

        if (wallcount == 2) {
            if (!cellMustHavePath) return LogicStatus.STYMIED;
            setUnknowns(thing,EdgeState.PATH);
            return LogicStatus.LOGICED;
        }

        return LogicStatus.STYMIED;
    }
}
