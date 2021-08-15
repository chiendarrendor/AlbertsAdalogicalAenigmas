import grid.logic.flatten.FlattenLogicer;

public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b) {
        // no futon pair may hava a pillow north of its futon (handled in PossibleFutons constructor)

        // aisle must be connected
        addLogicStep(new ConnectedAisleLogicStep());


        b.forEachCell((x,y)->{
            // numbered pillars show exact # of pillows H/V adjacent
            if (b.hasPillar(x,y) && b.hasNumericPillar(x,y)) {
                addLogicStep(new NumberedPillarLogicStep(b,x,y,b.getNumericPillarValue(x,y)));
            }

            // integrate cells with futon cells
            addLogicStep(new FutonAndCellLogicStep(x,y));


            if (x == b.getWidth()-1) return;
            if (y == b.getHeight() - 1) return;
            // no 2x2 aisle regions
            addLogicStep(new TwoByTwoAisleLogicStep(x,y));
        });



        // every set futon pair must be adjacent to at lest one aisle
        addLogicStep(new AisleAdjacencyLogicStep());


    }
}
