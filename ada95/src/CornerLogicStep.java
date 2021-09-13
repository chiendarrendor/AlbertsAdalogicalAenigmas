import grid.logic.LogicStep;
import grid.puzzlebits.EdgeContainer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class CornerLogicStep implements LogicStep<Board> {
    Collection<EdgeContainer.EdgeCoord> edges;
    public CornerLogicStep(Collection<EdgeContainer.EdgeCoord> edges) { this.edges = edges; }

    protected int pathcount = 0;
    protected int wallcount = 0;
    protected List<EdgeContainer.EdgeCoord> unknowns = new ArrayList<>();

    protected void calculateEdgeState(Board thing) {
        pathcount = 0;
        wallcount = 0;
        unknowns.clear();
        for(EdgeContainer.EdgeCoord edge : edges) {
            switch(thing.getEdge(edge.x,edge.y,edge.isV)) {
                case PATH: ++pathcount; break;
                case WALL: ++wallcount; break;
                case UNKNOWN: unknowns.add(edge); break;
            }
        }
    }
}
