import grid.logic.flatten.FlattenLogicer;

public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b) {
        b.forEachCell((x,y)->{
            GroundState gs = b.getGroundState(x,y);
            if (gs.isTarget()) addLogicStep(new TargetLogicStep(gs,b.illluminating(x,y)));
            if (gs == GroundState.TILE) addLogicStep(new TileLogicStep(x,y,b.illluminating(x,y)));
        });
    }
}
