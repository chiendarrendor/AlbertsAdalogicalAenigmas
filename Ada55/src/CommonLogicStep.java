import grid.graph.GridGraph;
import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

import java.awt.*;
import java.util.Set;
import java.util.stream.Collectors;

abstract public class CommonLogicStep implements LogicStep<Board> {
    private static class Reference implements GridGraph.GridReference {
        Board b;
        boolean isMin;
        public Reference(Board b,boolean isMin) { this.b = b; this.isMin = isMin; }
        public int getWidth() { return b.getWidth(); }
        public int getHeight() { return b.getHeight(); }
        public boolean isIncludedCell(int x,int y) { return true; }
        public boolean edgeExitsEast(int x,int y) {
            EdgeState es = b.getEdge(x,y, Direction.EAST);
            if (es == EdgeState.WALL) return false;
            if (es == EdgeState.PATH) return true;
            return !isMin;
        }
        public boolean edgeExitsSouth(int x,int y) {
            EdgeState es = b.getEdge(x,y, Direction.SOUTH);
            if (es == EdgeState.WALL) return false;
            if (es == EdgeState.PATH) return true;
            return !isMin;
        }
    }

    abstract public LogicStatus applyToGroup(Board thing,Set<Point> cells,Set<Point>numbers,GridGraph gg);

    boolean isMin;
    public CommonLogicStep(boolean isMin) { this.isMin = isMin;}
    public LogicStatus apply(Board thing) {
        GridGraph gg = new GridGraph(new Reference(thing,isMin));

        LogicStatus result = LogicStatus.STYMIED;
        for (Set<Point> group : gg.connectedSets()) {
            Set<Point> numbers = group.stream().filter(p->thing.hasNumber(p.x,p.y)).collect(Collectors.toSet());
            LogicStatus item = applyToGroup(thing,group,numbers,gg);
            if (item == LogicStatus.CONTRADICTION) return LogicStatus.CONTRADICTION;
            if (item == LogicStatus.LOGICED) return LogicStatus.LOGICED; // logic may invalidate another group.
        }
        return result;
    }
}




