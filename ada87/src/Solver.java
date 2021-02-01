import grid.logic.flatten.FlattenLogicer;
import grid.logic.simple.Logicer;

public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b) {
        for(RegionId rid : b.getRegions()) {
            addLogicStep(new UpdatePatternFromBoardLogicStep(rid));
        }

        for (Character pid : b.getPatternIds()) {
            addLogicStep(new UpdatePatternConnectivityLogicStep(pid));
        }

        addLogicStep(new ValidatePatternUniquenessLogicStep());

        for(RegionId rid : b.getRegions()) {
            addLogicStep(new UpdateBoardFromPatternLogicStep(rid));
        }

        for (Character pid : b.getPatternIds()) {
            addLogicStep(new UpdatePatternMinMaxLogicStep(pid));
        }


    }
}
