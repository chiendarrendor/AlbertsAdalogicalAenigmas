import grid.logic.flatten.FlattenLogicer;

public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b) {

        // iterate over Corner Space
        for (int y = 0 ; y < b.getCornerSpace().getCornerSpaceHeight() ; ++y) {
            for (int x = 0 ; x < b.getCornerSpace().getCornerSpaceWidth() ; ++x) {
                if (b.hasDotInCorner(x,y)) {
                    addLogicStep(new DottedCornerLogicStep(b.getCornerSpace().getCornerEdges(x,y)));
                } else {
                    addLogicStep(new NonDottedCornerLogicStep(b.getCornerSpace().getCornerEdges(x,y)));
                }
            }
        }
        addLogicStep(new DistrictConnectivityLogicStep());
    }
}
