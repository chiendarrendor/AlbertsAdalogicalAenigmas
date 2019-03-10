import grid.logic.LogicStatus;
import grid.logic.LogicStep;
import grid.logic.flatten.FlattenLogicer;
import grid.solverrecipes.genericloopyflatten.LoopyBoard;

import java.util.List;

public class Solver extends FlattenLogicer<Board> {
    private static class StepFacade implements LogicStep<Board> {
        LogicStep<LoopyBoard> substep;
        @Override public String toString() { return "StepFacade for " + substep; }
        public StepFacade(LogicStep<LoopyBoard> substep) { this.substep = substep; }
        public LogicStatus apply(Board b) {
            LogicStatus status = substep.apply(b.subboard);
//            System.out.println("apply to substep: " + substep + " " + status);
            return status;
        }
    }


    public Solver(Board b) {
        List<LogicStep<LoopyBoard>> lbsteps = b.subboard.getLogic();
        lbsteps.forEach(step->addLogicStep(new StepFacade(step)));

        b.forEachCell((x,y)->{
            if (!b.hasClue(x,y)) return;
            addLogicStep(new BrickSpecificClueLogicStep(b.getClue(x,y),b.cells.getCell(x,y)));
        });

    }
}
