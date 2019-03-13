import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.graph.GridGraph;
import grid.lambda.CellLambda;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.StandardFlattenSolvable;
import grid.puzzlebits.CellContainer;

import java.awt.Point;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Board implements StandardFlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Deep CellContainer<CellSet> cells;
    @Shallow Set<Character> regionids = new HashSet<>();
    @Shallow GridGraph regiongraph;

    private class MyGridReference implements GridGraph.GridReference {
        public boolean edgeExitsEast(int x,int y) { return getRegionId(x,y) == getRegionId(x+1,y); }
        public boolean edgeExitsSouth(int x,int y) { return getRegionId(x,y) == getRegionId(x,y+1); }
        public int getHeight() { return Board.this.getHeight(); }
        public int getWidth() { return Board.this.getWidth(); }
        public boolean isIncludedCell(int x,int y) { return true; }
    }




    public Board(String fname) {
        gfr = new GridFileReader(fname);

        regiongraph = new GridGraph(new MyGridReference());
        for (Set<Point> conset : regiongraph.connectedSets() ) {
            Point apoint = conset.iterator().next();
            char myrid = getRegionId(apoint.x,apoint.y);
            if (regionids.contains(myrid)) throw new RuntimeException("Board contains nonconnected region " + myrid);
            regionids.add(myrid);
        }

        cells = new CellContainer<CellSet>(getWidth(),getHeight(),(x,y)-> {
            Set<Point> region = regiongraph.connectedSetOf(new Point(x,y));
            CellSet result = new CellSet(region.size());
            if (hasNumber(x,y)) result.is(getNumber(x,y));
            return result;
        }, (x,y,old) -> new CellSet(old) );
    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }


    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public boolean onBoard(int x,int y) { return gfr.inBounds(x,y); }
    public String getClue() { return gfr.getVar("CLUES"); }
    public String getSolution() { return gfr.getVar("SOLUTION"); }
    public boolean hasNumber(int x,int y) { return !gfr.getBlock("NUMBERS")[x][y].equals("."); }
    public int getNumber(int x,int y) { return Integer.parseInt( gfr.getBlock("NUMBERS")[x][y]); }
    public boolean isSpecial(int x,int y) { return gfr.getBlock("SPECIALS")[x][y].charAt(0) != '.'; }
    public char getRegionId(int x,int y) { return gfr.getBlock("REGIONS")[x][y].charAt(0); }

    public CellSet getCellSet(int x,int y) { return cells.getCell(x,y); }

    public List<Set<Point>> getRegionPoints() { return regiongraph.connectedSets(); }

    private static class MyMove {
        int x;
        int y;
        boolean is;
        int number;

        public MyMove(int x,int y,boolean is,int number) { this.x = x; this.y = y; this.is = is; this.number = number; }
        public boolean applyMove(Board b) {
            if (!is) {
                b.getCellSet(x,y).isNot(number);
                return true;
            } else {
                if (!b.getCellSet(x,y).has(number)) return false;
                b.getCellSet(x,y).is(number);
                return true;
            }
        }
    }

    public boolean applyMove(Object o) { return ((MyMove)o).applyMove(this); }
    public boolean isComplete() {
        return CellLambda
                .stream(getWidth(),getHeight())
                .allMatch((p)->getCellSet(p.x,p.y).isDone());
    }

    @Override public FlattenSolvableTuple<Board> getOneTuple(int x, int y) {
        CellSet cs = getCellSet(x,y);
        if (cs.isDone()) return null;
        FlattenSolvableTuple<Board> result = new FlattenSolvableTuple<>();
        cs.stream()
                .forEach(num-> {
                    Board nb = new Board(this);
                    MyMove pro = new MyMove(x,y,true,num);
                    MyMove anti = new MyMove(x,y,false,num);
                    pro.applyMove(nb);
                    result.addTuple(nb,anti);
                });

        return result;
    }
}
