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


    @Override public LogicStatus apply(SingleLoopBoard thing) {
        processEdges(thing);
        if (pathcount > 2) return LogicStatus.CONTRADICTION;
        if (pathcount == 1 && unknowns.size() == 0) return LogicStatus.CONTRADICTION;
        if (unknowns.size() == 0) {
            if (cellMustHavePath && wallcount == 4) return LogicStatus.CONTRADICTION;
            return LogicStatus.STYMIED;
        }
        // if we get here, we have at least one unknown.
        // there's only one way to tell if we have a cell with no path
        if (wallcount == 3) {
            if (cellMustHavePath) return LogicStatus.CONTRADICTION;
            setUnknowns(thing,EdgeState.WALL);
            return LogicStatus.LOGICED;
        }

        if (wallcount == 2) {
            if (cellMustHavePath) {
                setUnknowns(thing,EdgeState.PATH);
                return LogicStatus.LOGICED;
            }
            return LogicStatus.STYMIED;
        }

        if (pathcount == 2) {
            setUnknowns(thing,EdgeState.WALL);
            return LogicStatus.LOGICED;
        }

        return LogicStatus.STYMIED;
    }
}
