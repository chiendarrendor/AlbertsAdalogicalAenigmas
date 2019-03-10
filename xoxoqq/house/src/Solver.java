import grid.logic.flatten.FlattenLogicer;

public class Solver extends FlattenLogicer<Board> {
    private boolean hasUnknownQuad(Board b,int x,int y) {
        if (!b.onBoard(x,y) || !b.onBoard(x+1,y) || !b.onBoard(x,y+1) || !b.onBoard(x+1,y+1)) return false;
        if (b.getCell(x,y) != CellType.UNKNOWN || b.getCell(x+1,y) != CellType.UNKNOWN ||
                b.getCell(x,y+1) != CellType.UNKNOWN || b.getCell(x+1,y+1) != CellType.UNKNOWN) return false;

        return true;
    }



    public Solver(Board b) {
        b.forEachCell((x,y)->{
            if (b.hasNumber(x,y)) addLogicStep(new WhiteRegionLogicStep(x,y,b.getNumber(x,y)));
            if (hasUnknownQuad(b,x,y)) addLogicStep(new BlackQuadLogicStep(x,y));
        });
        addLogicStep(new BlackConnectedLogicStep());
        addLogicStep(new NoEmptyWhiteRegionLogicStep());
    }


}
