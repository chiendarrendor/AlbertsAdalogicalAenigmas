import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.lambda.CellLambda;
import grid.lambda.XYLambda;
import grid.logic.flatten.FlattenSolvable;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.puzzlebits.CellContainer;
import grid.puzzlebits.Direction;
import grid.spring.GridFrame;

import java.util.ArrayList;
import java.util.List;

public class Board implements FlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Shallow CellContainer<Integer> regionids;
    @Deep RegionGraph rg;
    @Shallow int unknowns;


    public Board(String filename) {
        gfr = new GridFileReader(filename);
        regionids = new CellContainer<Integer>(getWidth(),getHeight(),
                (x,y)->gfr.getBlock("REGIONS")[x][y].charAt(0) == '.' ? -1 : Integer.parseInt(gfr.getBlock("REGIONS")[x][y]));

        rg = new RegionGraph(getWidth(),getHeight());
        forEachCell((x,y)-> {
            rg.cellIsRegion(x,y,getRegionId(x,y));
            if (hasLetter(x,y)) {
                rg.cellHasLetter(x,y,getLetter(x,y),getletterDir(x,y));
            }
            if (x < getWidth() - 1 && getRegionId(x,y) != getRegionId(x+1,y)) {
                rg.edgeIsBorder(x,y,Direction.EAST,getRegionId(x+1,y));
            }
            if (y < getHeight() - 1 && getRegionId(x,y) != getRegionId(x,y+1)) {
                rg.edgeIsBorder(x,y,Direction.SOUTH,getRegionId(x,y+1));
            }
        });
        rg.calculatePairs();
        unknowns = rg.edges.size();


    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public int getRegionId(int x, int y) { return regionids.getCell(x,y); }
    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }

    public boolean hasLetter(int x,int y) { return !".".equals(gfr.getBlock("LETTERS")[x][y]); }
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public Direction getletterDir(int x,int y) {
        if (gfr.getBlock("LETTERS")[x][y].length() == 1) return null;
        return Direction.fromShort(gfr.getBlock("LETTERS")[x][y].substring(1,2));
    }

    public void setRegionEdge(int rid1,int rid2,EdgeState es) {
        Edge e = rg.regions.get(rid1).edges.get(rid2);
        e.setState(es,true);
        unknowns--;
    }

    public EdgeState getRegionEdge(int rid1, int rid2) {
        Edge e = rg.regions.get(rid1).edges.get(rid2);
        return e.getState();
    }



    @Override public boolean isComplete() {
        return unknowns == 0;
    }

    private static class MyMove {
        int rid1;
        int rid2;
        EdgeState es;

        public MyMove(int rid1,int rid2,EdgeState es) { this.rid1 = rid1; this.rid2 = rid2; this.es = es; }
        public boolean applyMove(Board thing) {
            if (thing.getRegionEdge(rid1,rid2) != EdgeState.UNKNOWN) return es == thing.getRegionEdge(rid1,rid2);
            thing.setRegionEdge(rid1,rid2,es);
            return true;
        }
    }

    @Override public boolean applyMove(Object o) {
        return ((MyMove)o).applyMove(this);
    }

    private List<FlattenSolvableTuple<Board>> getSuccessorTuples(boolean onlyOne) {
        List<FlattenSolvableTuple<Board>> result = new ArrayList<>();

        for (Edge e : rg.edges) {
            if (e.getState() != EdgeState.UNKNOWN) continue;
            Board b1 = new Board(this);
            Board b2 = new Board(this);
            MyMove mm1 = new MyMove(e.regionid1,e.regionid2,EdgeState.CLOSED);
            MyMove mm2 = new MyMove(e.regionid1,e.regionid2,EdgeState.OPEN);
            mm1.applyMove(b1);
            mm2.applyMove(b2);
            result.add(new FlattenSolvableTuple<Board>(b1,mm1,b2,mm2));
            if (onlyOne) break;
        }

        return result;
    }

    @Override public List<FlattenSolvableTuple<Board>> getSuccessorTuples() { return getSuccessorTuples(false); }

    @Override public List<Board> guessAlternatives() {
        FlattenSolvableTuple<Board> fst = getSuccessorTuples(true).get(0);
        return fst.choices;
    }
}
