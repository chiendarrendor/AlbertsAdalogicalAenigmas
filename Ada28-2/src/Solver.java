import grid.logic.flatten.FlattenLogicer;

public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b) {
        b.forEachCell((x,y)-> {
            if (b.isVista(x,y)) {
                addLogicStep(new VistaLogicStep(x,y,b.vistaNumber(x,y)));
            } else {
                addLogicStep(new PotentialTreeLogicStep(x,y));
            }
        });
        addLogicStep(new PathConnectivityLogicStep());
    }
}
