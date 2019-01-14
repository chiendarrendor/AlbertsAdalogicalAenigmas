import grid.copycon.CopyCon;
import grid.copycon.Ignore;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.file.SubReader;
import grid.lambda.CellLambda;
import grid.lambda.XYLambda;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.MultiFlattenSolvable;

import java.util.ArrayList;
import java.util.List;

public class Board implements MultiFlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Ignore private static final int boardcount = 2;
    @Ignore private SubBoard[] subboards = new SubBoard[boardcount];


    public Board(String fname) {
        gfr = new GridFileReader(fname);
        for (int i = 0 ; i < boardcount; ++i) this.subboards[i] =
                new SubBoard(new SubReader(gfr,i*getWidth(),0,getWidth(),getHeight()));



    }

    public Board(Board right) {
        CopyCon.copy(this,right);
        for(int i = 0; i < boardcount ; ++i) this.subboards[i] = new SubBoard(right.subboards[i]);
    }

    public int getWidth() { return gfr.getWidth() / boardcount; }
    public int getHeight() { return gfr.getHeight(); }
    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }

    public int getBoardCount() { return boardcount; }
    public SubBoard getSubBoard(int boardid) { return subboards[boardid]; }
    public int getUnknownCount() {
        int sum = 0;
        for (int i = 0 ; i < boardcount ; ++i) sum += getSubBoard(i).getUnknowns();
        return sum;
    }

    @Override public boolean isComplete() { return getUnknownCount() == 0; }

    private static class MyMove {
        int subid;
        int x;
        int y;
        LightState ls;
        public MyMove(int subid, int x,int y,LightState ls) { this.subid = subid; this.x = x; this.y =y; this.ls = ls; }

        public boolean applyMove(Board b) {
            SubBoard sb = b.getSubBoard(subid);
            if (sb.getLightState(x,y) != LightState.UNKNOWN) return sb.getLightState(x,y) == ls;
            sb.setLightState(x,y,ls);
            return true;
        }
    }

    @Override public boolean applyMove(Object o) {
        return ((MyMove)o).applyMove(this);
    }

    @Override public List<FlattenSolvableTuple<Board>> getTuplesForCell(int x, int y) {
        List<FlattenSolvableTuple<Board>> result = new ArrayList<>();

        for (int id = 0 ; id < getBoardCount() ; ++id) {
            SubBoard sb = getSubBoard(id);
            if (sb.getLightState(x,y) != LightState.UNKNOWN) continue;
            Board b1 = new Board(this);
            Board b2 = new Board( this);
            MyMove mm1 = new MyMove(id,x,y,LightState.NOLIGHT);
            MyMove mm2 = new MyMove(id,x,y,LightState.LIGHT);
            mm1.applyMove(b1);
            mm2.applyMove(b2);
            result.add(new FlattenSolvableTuple<Board>(b1,mm1,b2,mm2));
        }

        return result;
    }


}
