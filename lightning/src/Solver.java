import grid.logic.flatten.FlattenLogicer;

public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b) {
        for (int id = 0 ; id < b.getBoardCount() ; ++id) {
            int fid = id;
            SubBoard sb = b.getSubBoard(id);

            sb.forEachCell((x,y)-> {
                if (sb.isNumber(x,y)) {
                    addLogicStep(new NumberLogicStep(fid,x,y,sb));
                }
                if (!sb.isBlocker(x,y)) {
                    addLogicStep(new CellLogicStep(fid,x,y,sb));
                }
            });

        }
    }
}
