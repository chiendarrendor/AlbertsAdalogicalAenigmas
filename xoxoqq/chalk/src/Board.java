import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.lambda.CellLambda;
import grid.lambda.XYLambda;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.StandardFlattenSolvable;
import grid.puzzlebits.CellContainer;
import org.apache.commons.lang3.NotImplementedException;

import java.awt.Point;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Board implements StandardFlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Shallow Map<Character,Region> regions = new HashMap<>();
    @Shallow CellContainer<Region> cellregions;
    @Deep CellContainer<CellSet> cells;

    private Region gocRegion(char id) {
        if (!regions.containsKey(id)) {
            regions.put(id,new Region(id));
        }
        return regions.get(id);
    }

    public Board(String fname) {
        gfr = new GridFileReader(fname);

        cellregions = new CellContainer<Region>(getWidth(),getHeight(),(x,y)->{
           char rcellid = gfr.getBlock("REGIONS")[x][y].charAt(0);
           if (rcellid == '.') return null;
           Region r = gocRegion(rcellid);
           r.addCell(new Point(x,y));
           return r;
        });

        cells = new CellContainer<CellSet>(getWidth(),getHeight(),
            (x,y)-> getRegion(x,y) == null ? null : new CellSet(getRegion(x,y).size()),
            (x,y,old)->old == null ? null : new CellSet(old)
        );

        forEachCell((x,y)-> {
            String istr = gfr.getBlock("INITIALS")[x][y];
            if (istr.equals(".")) return;
            int inum = Integer.parseInt(istr);
            getCellSet(x,y).is(inum);

        });


    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public boolean onBoard(int x,int y) { return gfr.inBounds(x,y); }
    public boolean isSpecial(int x,int y) { return gfr.getBlock("SPECIALS")[x][y].charAt(0) == 'o'; }
    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }
    public Collection<Region> getRegions() { return regions.values(); }
    public Region getRegion(char id) { return regions.get(id); }
    public Region getRegion(int x,int y) { return cellregions.getCell(x,y); }
    public CellSet getCellSet(int x,int y) { return cells.getCell(x,y); }


    // FlattenSolvable
    @Override public boolean isComplete() {
        return CellLambda.stream(getWidth(),getHeight())
                .filter(p->getRegion(p.x,p.y) != null)
                .allMatch(p->getCellSet(p.x,p.y).size() == 1);
    }

    private static class MyMove {
        int x;
        int y;
        int num;
        boolean isNumber;
        public MyMove(int x,int y,int num,boolean isNumber) {this.x = x; this.y = y; this.num = num; this.isNumber = isNumber; }
        public boolean applyMove(Board b) {
            if (isNumber) return b.getCellSet(x,y).is(num);
            b.getCellSet(x,y).isNot(num);
            return true;
        }

    }


    @Override public boolean applyMove(Object o) { return ((MyMove)o).applyMove(this);  }

    @Override public FlattenSolvableTuple<Board> getOneTuple(int x, int y) {
        if (getRegion(x,y) == null) return null;
        CellSet cs = getCellSet(x,y);
        if (cs.size() < 2) return null;
        FlattenSolvableTuple<Board> fst = new FlattenSolvableTuple<>();
        for (Integer i : cs) {
            Board b1 = new Board(this);
            Board b2 = new Board( this);
            MyMove mm1 = new MyMove(x,y,i,true);
            MyMove mm2 = new MyMove(x,y,i,false);
            mm1.applyMove(b1);
            mm2.applyMove(b2);

            fst.addTuple(b1,mm2);
            fst.addTuple(b2,mm1);
        }
        return fst;
    }

}
