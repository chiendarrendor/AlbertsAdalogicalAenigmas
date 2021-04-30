import grid.logic.flatten.FlattenLogicer;

public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b) {
        b.forEachCell((x,y)->{
  //          if (b.hasClue(x,y)) addLogicStep(new ClueLogicStep(x,y,b.getClue(x,y)));
  //          else addLogicStep(new CellLogicStep(x,y));
        });

        addLogicStep(new ConnectedPathLogicStep());
    }
}
