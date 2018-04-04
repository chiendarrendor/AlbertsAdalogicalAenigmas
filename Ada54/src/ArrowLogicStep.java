import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.puzzlebits.Direction;

public class ArrowLogicStep implements LogicStep<Board> {
    int x;
    int y;
    Direction arrowdir;
    public ArrowLogicStep(int x, int y, Direction arrowdir) {
        this.x = x;
        this.y = y;
        this.arrowdir = arrowdir;
    }

    /**
     * This is a very simple process.
     * This cell must have a path going straight through it in the direction of the arrow.
     * @param thing
     * @return
     */
    public LogicStatus apply(Board thing) {
        if (thing.isCellComplete(x,y)) return LogicStatus.STYMIED;
        thing.setCellComplete(x,y);

        // fix the outbound arrow.
        EdgeInfo out = thing.getEdge(x,y,arrowdir);
        if (!out.canGo(arrowdir)) return LogicStatus.CONTRADICTION;
        thing.useEdge(x,y,arrowdir);
        out.removeInbound(arrowdir);

        // fix the inbound arrow.
        Direction behind = arrowdir.getOpp();
        EdgeInfo in = thing.getEdge(x,y,behind);
        if (!in.canGo(arrowdir)) return LogicStatus.CONTRADICTION;
        thing.useEdge(x,y,behind);
        in.removeOutbound(behind);

        // fix right wall
        Direction wdir = arrowdir.right();
        EdgeInfo rt = thing.getEdge(x,y,wdir);
        if (rt.isUsed()) return LogicStatus.CONTRADICTION;
        rt.clear();

        // fix left wall
        Direction ldir = wdir.getOpp();
        EdgeInfo lt = thing.getEdge(x,y,ldir);
        if (lt.isUsed()) return LogicStatus.CONTRADICTION;
        lt.clear();

        return LogicStatus.LOGICED;
    }
}
