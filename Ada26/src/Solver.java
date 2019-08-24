import grid.logic.flatten.FlattenLogicer;
import grid.puzzlebits.Direction;

public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b) {
        b.forEachCell((x,y)->{
            addLogicStep(new CellLogicStep(x,y));
            if (b.hasNumber(x,y)) addLogicStep(new RegionLogicStep(x,y));
        });

        if (b.fakeinfo != null) {
            for (int x = 0; x < b.getWidth(); ++x) {
                addLogicStep(new LineLiarsLogicStep(b, x, 0, Direction.SOUTH, b.getHeight(), b.fakeinfo.colmin, b.fakeinfo.colmax));
            }
            for (int y = 0 ; y < b.getHeight() ; ++y) {
                addLogicStep(new LineLiarsLogicStep(b,0,y,Direction.EAST,b.getWidth(),b.fakeinfo.rowmin,b.fakeinfo.rowmax));
            }
        }
    }
}
