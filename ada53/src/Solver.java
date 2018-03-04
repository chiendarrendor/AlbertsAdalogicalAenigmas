import grid.logic.flatten.FlattenLogicer;

public class Solver extends FlattenLogicer<Board>
{
    public Solver(Board b) {
        b.circles.keySet().stream().forEach((p)->addLogicStep(new CircleLogicStep(p)));
        addLogicStep(new RectangleGroupLogicStep());
    }
}
