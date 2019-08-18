import grid.logic.flatten.FlattenLogicer;

public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b) {
        b.forEachCell((x,y)->{
            addLogicStep(new CellLogicStep(x,y));
            if(b.isPossession(x,y)) addLogicStep(new PossessionLogicStep(x,y));
        });

        for(char regid : b.getRegions()) {
            addLogicStep(new RegionLogicStep(regid,b.getRegionSet(regid),b));
        }
        addLogicStep(new RequiredOverrunLogicStep());
    }
}
