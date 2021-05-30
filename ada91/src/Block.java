import grid.puzzlebits.Direction;

import java.util.ArrayList;
import java.util.List;

public class Block {
    List<CellList> rows = new ArrayList<>();
    List<CellList> columns = new ArrayList<>();

    public Block(int ulx, int uly, int size) {
        for (int i = 0 ; i < size ; ++i) {
            rows.add(new CellList(ulx,uly+i,size, Direction.EAST));
            columns.add(new CellList(ulx+i,uly,size,Direction.SOUTH));
        }
    }

    public interface ListOperator { public void go(CellList cl); }
    public void forEachRow(ListOperator op) {
        for(CellList cl : rows) op.go(cl);
    }
    public void forEachColumn(ListOperator op) {
        for (CellList cl : columns) op.go(cl);
    }


}
