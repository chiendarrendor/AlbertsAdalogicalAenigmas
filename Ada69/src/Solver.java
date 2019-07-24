import grid.logic.flatten.FlattenLogicer;
import grid.puzzlebits.Direction;

import java.util.HashMap;
import java.util.Map;

public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b) {
        Map<Character,RegionLogicStep> regionsteps = new HashMap<>();

        b.forEachCell((x,y)->{
            if (x == 0) addLogicStep(new LineLogicStep(x,y,b.getWidth(), Direction.EAST,CellState.VERTICAL));
            if (y == 0) addLogicStep(new LineLogicStep(x,y,b.getHeight(),Direction.SOUTH,CellState.HORIZONTAL));


            char rid = b.getRegion(x,y);
            if(!regionsteps.containsKey(rid)) {
                RegionLogicStep newrls = new RegionLogicStep();
                addLogicStep(newrls);
                regionsteps.put(rid,newrls);
            }
            regionsteps.get(rid).addCell(x,y);
            if(b.hasNumber(x,y)) regionsteps.get(rid).setNumber(b.getNumber(x,y));
        });

    }
}
