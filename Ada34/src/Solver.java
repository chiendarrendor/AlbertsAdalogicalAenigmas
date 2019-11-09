import grid.logic.flatten.FlattenLogicer;

public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b) {
        b.forEachCell((x,y)-> {
            if(b.isArrowClue(x,y)) addLogicStep(new ArrowClueLogicStep(x,y,b.getArrowClue(x,y)));
            if(b.isNumberClue(x,y)) addLogicStep(new NumberClueCoreLogicStep(x,y,b.getNumberClue(x,y)));
            addLogicStep(new TerminalAdjacentLogicStep(x,y));
            addLogicStep(new BendAjacentLogicStep(x,y));
            addLogicStep(new CellPathLogicStep(x,y));
        });

        b.forEachCell((x,y)-> {
            if (b.isNumberClue(x,y)) addLogicStep(new NumberClueArmLogicStep(x,y,b.getNumberClue(x,y)));
        });

        b.forEachCell((x,y)-> {
            addLogicStep(new CellStateLogicStep(x,y));
        });

        addLogicStep(new RegionPathLogicStep());
    }
}
