import grid.logic.flatten.FlattenLogicer;
import grid.puzzlebits.Direction;
import grid.solverrecipes.singleloopflatten.CellPathLogicStep;
import grid.solverrecipes.singleloopflatten.SinglePathLogicStep;

public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b) {
        b.forEachCell((x,y)-> {
            addLogicStep(new CellPathLogicStep<Board>(x,y,b.hasClue(x,y)));
            char clue = b.getRawClue(x,y);
            if (clue == '@') addLogicStep(new CellCenterExtendLogicStep(x,y));
            if (clue == 'E') addLogicStep(new CellEdgeExtendLogicStep(x,y, Direction.EAST));
            if (clue == 'S') addLogicStep(new CellEdgeExtendLogicStep(x,y,Direction.SOUTH));
        });
        addLogicStep(new SinglePathLogicStep<Board>());
    }
}
