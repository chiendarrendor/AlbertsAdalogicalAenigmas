import grid.logic.flatten.FlattenLogicer;

public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b) {
        b.forEachCell((x,y)->{
            addLogicStep(new adjacentCellLogicStep(b,x,y));
            if (b.inBounds(x,y+1) && b.getRegionId(x,y) == b.getRegionId(x,y+1)) {
                addLogicStep(new regionVerticalLogicStep(x,y));
            }
        });

        for (char rid : b.getRegionIds()) {
            addLogicStep(new RegionLogicStep(b.getCellsForRegion(rid)));
        }

    }
}
