import grid.copycon.CopyCon;
import grid.copycon.Ignore;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.lambda.CellLambda;
import grid.lambda.XYLambda;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.MultiFlattenSolvable;
import grid.puzzlebits.Direction;
import grid.solverrecipes.singleloopflatten.EdgeState;
import grid.solverrecipes.singleloopflatten.SingleLoopBoard;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Board extends SingleLoopBoard<Board> implements MultiFlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Ignore Map<Character,RegionStatus> statuses;
    @Shallow Map<Character,Region> regions;

    public Board(String fname) {
        gfr = new GridFileReader(fname);
        init();

        statuses = new HashMap<>();
        regions = new HashMap<>();

        forEachCell((x,y)-> {
            char rid = getRegionId(x,y);
            if (!regions.containsKey(rid)) {
                statuses.put(rid,RegionStatus.UNKNOWN);
                regions.put(rid,new Region(rid));
            }
            if (isSun(x,y)) regions.get(rid).addSun(new Point(x,y));
            else if (isMoon(x,y)) regions.get(rid).addMoon(new Point(x,y));
            else regions.get(rid).addVoid(new Point(x,y));
        });





    }

    public Board(Board right) {
        CopyCon.copy(this,right);
        statuses = new HashMap<>(right.statuses);
    }


    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.'; }
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public char getRegionId(int x,int y) { return gfr.getBlock("REGIONS")[x][y].charAt(0); }
    public boolean isSun(int x,int y) { return gfr.getBlock("COSMIC")[x][y].charAt(0) == 'S'; }
    public boolean isMoon(int x,int y) { return gfr.getBlock("COSMIC")[x][y].charAt(0) == 'M'; }
    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }
    public boolean onBoard(int x,int y) { return gfr.inBounds(x,y); }
    public boolean onBoard(Point p) { return gfr.inBounds(p); }

    public void setRegionStatus(char rid,RegionStatus stat) { statuses.put(rid,stat); }
    public RegionStatus getRegionStatus(char rid) { return statuses.get(rid); }
    public Region getRegion(char rid) { return regions.get(rid); }


    public boolean isComplete() {
        if (statuses.values().stream().filter(x->x==RegionStatus.UNKNOWN).count() > 0) return false;
        return getUnknownCount() == 0;
    }

    private static class MyMove {
        boolean isRegion;
        char rid;
        RegionStatus rstat;

        int x;
        int y;
        Direction d;
        EdgeState es;

        public MyMove(char rid,RegionStatus rstat) { isRegion = true; this.rid = rid; this.rstat = rstat; }
        public MyMove(int x,int y,Direction d,EdgeState es) { isRegion = false; this.x = x; this.y = y; this.d = d; this.es = es; }

        public boolean applyMove(Board thing) {
            if (isRegion) {
                if (thing.statuses.get(rid) != RegionStatus.UNKNOWN) {
                    return thing.statuses.get(rid) == rstat;
                }
                thing.statuses.put(rid,rstat);
                return true;
            }

            EdgeState cures = thing.getEdge(x,y,d);
            if (cures != EdgeState.UNKNOWN) {
                return cures == es;
            }
            thing.setEdge(x,y,d,es);
            return true;
        }
    }

    public boolean applyMove(Object o) { return ((MyMove)o).applyMove(this); }

    private FlattenSolvableTuple<Board> makeFST(MyMove mm1,MyMove mm2) {
        Board b1 = new Board(this);
        Board b2 = new Board(this);
        mm1.applyMove(b1);
        mm2.applyMove(b2);
        return new FlattenSolvableTuple<Board>(b1,mm1,b2,mm2);
    }



    private void getTupleForCellAndDir(List<FlattenSolvableTuple<Board>> list, int x, int y, Direction d) {
        EdgeState cures = getEdge(x,y,d);
        if (cures != EdgeState.UNKNOWN) return;
        MyMove mm1 = new MyMove(x,y,d,EdgeState.WALL);
        MyMove mm2 = new MyMove(x,y,d,EdgeState.PATH);
        list.add(makeFST(mm1,mm2));
    }

    @Override public List<FlattenSolvableTuple<Board>> getTuplesForCell(int x, int y) {
        List<FlattenSolvableTuple<Board>> result = new ArrayList<>();
        getTupleForCellAndDir(result,x,y,Direction.EAST);
        getTupleForCellAndDir(result,x,y,Direction.SOUTH);
        return result;
    }

    private List<FlattenSolvableTuple<Board>> getRegionTuples(boolean stopfirst) {
        List<FlattenSolvableTuple<Board>> result = new ArrayList<>();
        for(char rid : regions.keySet()) {
            if (statuses.get(rid) != RegionStatus.UNKNOWN) continue;
            result.add(makeFST(new MyMove(rid,RegionStatus.DAY),new MyMove(rid,RegionStatus.NIGHT)));
            if (stopfirst) return result;
        }
        return result;
    }


    @Override public List<FlattenSolvableTuple<Board>> getSuccessorTuples() {
        List<FlattenSolvableTuple<Board>> result = new ArrayList<>();
        result.addAll(getRegionTuples(false));
        result.addAll(MultiFlattenSolvable.super.getSuccessorTuples());

        return result;
    }

    @Override public List<Board> guessAlternatives() {
        List<FlattenSolvableTuple<Board>> regtuples = getRegionTuples(true);
        if (regtuples.size() > 0) {
            return regtuples.get(0).choices;
        }
        return MultiFlattenSolvable.super.guessAlternatives();
    }


}
