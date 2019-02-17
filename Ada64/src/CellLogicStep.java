import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;
import grid.solverrecipes.singleloopflatten.EdgeState;

import java.util.Vector;

public class CellLogicStep implements LogicStep<Board> {
    int x;
    int y;
    int count;

    public CellLogicStep(Board b, int x, int y) {
        this.x = x;
        this.y = y;
        this.count = b.isTerminal(x,y) ? 1 : 2;
    }

    Vector<Direction> unknowns = new Vector<>();
    int pathcount;
    int wallcount;

    private void processEdges(Board thing)
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

    private void setUnknowns(Board thing, EdgeState et) { for(Direction d : unknowns) thing.setEdge(x,y,d,et); }


    @Override public LogicStatus apply(Board thing) {
        processEdges(thing);
        if (pathcount > count) return LogicStatus.CONTRADICTION;
        if (pathcount + unknowns.size() < count) return LogicStatus.CONTRADICTION;
        if (unknowns.size() == 0) return LogicStatus.STYMIED;

        if (pathcount == count) {
            setUnknowns(thing,EdgeState.WALL);
            return LogicStatus.LOGICED;
        }

        if (pathcount + unknowns.size() == count) {
            setUnknowns(thing,EdgeState.PATH);
            return LogicStatus.LOGICED;
        }

        return LogicStatus.STYMIED;
    }

}
