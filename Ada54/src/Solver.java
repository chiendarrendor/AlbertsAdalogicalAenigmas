import grid.logic.flatten.FlattenLogicer;
import grid.puzzlebits.Direction;

public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b) {
        b.forEachCell((x,y) -> {
            if (b.hasBlock(x,y)) return;
            Direction arrowdir  = b.getArrow(x,y);
            if (arrowdir != null) {
                addLogicStep( new ArrowLogicStep(x,y,arrowdir));
            } else {
                addLogicStep( new WallsCloseInLogicStep(x,y));
                addLogicStep( new UsedLogicStep(x,y,EdgeSynopsis.USED_IN));
                addLogicStep( new UsedLogicStep(x,y,EdgeSynopsis.USED_OUT));
                addLogicStep( new UnusedIOCountLogicStep(x,y));
                b.getWinds(x,y).stream().forEach((w)->addLogicStep(new CrosswindsLogicStep(x,y,w)));
            }
        });
        addLogicStep(new LoopLogicStep());
    }
}
