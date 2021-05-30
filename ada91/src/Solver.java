import grid.logic.flatten.FlattenLogicer;

public class Solver extends FlattenLogicer<Board> {
    public Solver(Board b) {
        b.forEachBlock(bl->{
            bl.forEachRow(row->{
                addLogicStep(new HeightUniqueLogicStep(row.cells()));
                if (b.hasRowClue(row.start()))
                    addLogicStep(new TowerHidingLogicStep(row.cells(),b.getRowClue(row.start())));
                if (b.hasRowClue(row.end()))
                    addLogicStep(new TowerHidingLogicStep(row.reversed(),b.getRowClue(row.end())));
            });
            bl.forEachColumn(column->{
                addLogicStep(new HeightUniqueLogicStep(column.cells()));
                if (b.hasColumnClue(column.start()))
                    addLogicStep(new TowerHidingLogicStep(column.cells(),b.getColumnClue(column.start())));
                if (b.hasColumnClue(column.end()))
                    addLogicStep(new TowerHidingLogicStep(column.reversed(),b.getColumnClue(column.end())));
            });
        });
    }
}
