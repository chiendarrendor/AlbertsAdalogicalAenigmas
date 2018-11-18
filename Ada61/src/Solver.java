import grid.logic.flatten.FlattenLogicer;

public class Solver extends FlattenLogicer<Board> {

    public Solver(Board b) {
        for (int rid : b.rg.regions.keySet()) {
            addLogicStep(new RegionLogicStep(rid));
        }

        for (Edge e : b.rg.edges) {
            addLogicStep(new EdgeLogicStep(e.regionid1,e.regionid2));
        }

    }



}
