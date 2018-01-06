import grid.logic.flatten.FlattenLogicer;
import grid.puzzlebits.Direction;

public class Solver extends FlattenLogicer<Board>
{

    public Solver(Board b)
    {
        b.forEachCell((x,y) ->
                {
                    if (b.getClue(x,y) == '.') addLogicStep(new CellLogicStep(x, y));
                });


        for (int x = 0 ; x < b.getWidth() ; ++x) addLogicStep(new StripLogicStep(x,0, Direction.SOUTH,b.getHeight(),b.getVClue(x)));
        for (int y = 0 ; y < b.getHeight() ; ++y) addLogicStep(new StripLogicStep(0,y,Direction.EAST,b.getWidth(),b.getHClue(y)));


        addLogicStep(new SinglePathLogicStep());

    }

}
