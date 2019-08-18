import grid.lambda.LambdaInteger;
import grid.logic.LogicStatus;

public class RequiredOverrunLogicStep implements grid.logic.LogicStep<Board> {
    @Override public LogicStatus apply(Board thing) {
        LambdaInteger initialcount = new LambdaInteger(0);
        LambdaInteger requiredcount = new LambdaInteger(0);
        thing.forEachCell((x,y)->{
            CellType ct = thing.getCell(x,y);
            if (ct.getPathType() == PathType.INITIAL) initialcount.inc();
            if (ct.getPresenceType() == PresenceType.REQUIRED && ct.getPathType() != PathType.TERMINAL) requiredcount.inc();
        });

        return requiredcount.get() > initialcount.get() ? LogicStatus.CONTRADICTION : LogicStatus.STYMIED;
    }
}
