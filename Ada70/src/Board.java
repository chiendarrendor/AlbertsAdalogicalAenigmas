import grid.copycon.CopyCon;
import grid.copycon.Deep;
import grid.copycon.Shallow;
import grid.file.GridFileReader;
import grid.lambda.BooleanXYLambda;
import grid.lambda.CellLambda;
import grid.lambda.XYLambda;
import grid.logic.flatten.FlattenSolvable;
import grid.logic.flatten.FlattenSolvableTuple;
import grid.logic.flatten.MultiFlattenSolvable;
import grid.logic.flatten.StandardFlattenSolvable;
import grid.puzzlebits.CellContainer;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Board implements MultiFlattenSolvable<Board> {
    @Shallow GridFileReader gfr;
    @Deep CellContainer<CellType> cells;
    @Deep CellContainer<JumpSet> jumps;
    @Shallow Map<Character,Set<Point>> regions = new HashMap<>();
    @Shallow CellContainer<String> regionpairs;
    @Shallow CellContainer<JumpList> backreferences;

    private void regionalize(int x,int y) {
        char rid = getRegionId(x,y);
        if (!regions.containsKey(rid)) {
            regions.put(rid,new HashSet<>());
        }
        regions.get(rid).add(new Point(x,y));
    }

    public Set<Character> getRegions() { return regions.keySet(); }
    public Set<Point> getRegionSet(char rid) { return regions.get(rid); }


    public Board(String fname) {
        gfr = new GridFileReader(fname);
        backreferences = new CellContainer<JumpList>(getWidth(),getHeight(),(x,y)->new JumpList());

        cells = new CellContainer<CellType>(getWidth(),getHeight(),
                (x,y)-> {
                    regionalize(x,y);
                    if (isPossession(x,y)) {
                        return getPossession(x,y) > 0 ? CellType.INITIALFORBIDDEN : CellType.INITIAL;
                    }
                    return CellType.EMPTY;
                });
        jumps = new CellContainer<JumpSet>(getWidth(),getHeight(),
                (x,y)->{
                    if (!isPossession(x,y)) return null;
                    return new JumpSet(this,x,y,getPossession(x,y));
                },
                (x,y,r)-> r == null ? null : new JumpSet(r) );

        regionpairs = new CellContainer<String>(getWidth(),getHeight(), (x,y)->"" + getRegionId(x,y) + "-");



    }

    public Board(Board right) {
        CopyCon.copy(this,right);
    }

    public int getWidth() { return gfr.getWidth(); }
    public int getHeight() { return gfr.getHeight(); }
    public boolean onBoard(int x,int y) { return gfr.inBounds(x,y); }
    public char getRegionId(int x,int y) { return gfr.getBlock("REGIONS")[x][y].charAt(0); }
    public boolean hasLetter(int x,int y) { return getLetter(x,y) != '.'; }
    public char getLetter(int x,int y) { return gfr.getBlock("LETTERS")[x][y].charAt(0); }
    public boolean isPossession(int x,int y) { return gfr.getBlock("POSSESSIONS")[x][y].charAt(0) != '.'; }
    public int getPossession(int x,int y) { return Integer.parseInt(gfr.getBlock("POSSESSIONS")[x][y]); }
    public void forEachCell(XYLambda xyl) { CellLambda.forEachCell(getWidth(),getHeight(),xyl); }
    public boolean booleanForEachCell(BooleanXYLambda bxyl) { return CellLambda.terminatingForEachCell(getWidth(),getHeight(),bxyl); }

    public CellType getCell(int x,int y) { return cells.getCell(x,y); }
    public void setCell(int x,int y,CellType val) { cells.setCell(x,y,val);}
    public void setCell(int x,int y,PathType ptype) { cells.setCell(x,y,new CellType(cells.getCell(x,y),ptype)); }
    public void setCell(int x,int y,PresenceType ptype) { cells.setCell(x,y,new CellType(cells.getCell(x,y),ptype)); }

    public JumpSet getJumpSet(int x,int y) { return jumps.getCell(x,y); }

    public JumpList getBackReferences(int x,int y) { return backreferences.getCell(x,y); }

    public String getRegionPair(int x,int y) { return regionpairs.getCell(x,y); }
    public void setRegionPair(int x,int y,String s) { regionpairs.setCell(x,y,s);}

    @Override public boolean isComplete() { return booleanForEachCell((x,y)-> { return getCell(x,y).isComplete(); }); }

    private static class MyMove {
        Jump jump;
        boolean doset;
        public MyMove(Jump jump,boolean doset) { this.jump = jump; this.doset = doset; }
        public boolean applyMove(Board b) {
            JumpSet js = b.jumps.getCell(jump.base.x, jump.base.y);
            if (js.size() == 1 && jump == js.solo()) return true;
            if (b.getCell(jump.base.x,jump.base.y).getPathType() != PathType.INITIAL) return false;

            if (doset) {
                if (!jump.isLegal(b)) return false;
                if (!js.set(jump)) return false;
                jump.place(b);
                return true;
            } else {
                js.remove(jump);
            }
            return true;
        }
        public String toString() { return jump + ": " + doset; }
    }


    @Override public boolean applyMove(Object o) { return ((MyMove)o).applyMove(this); }

    public List<FlattenSolvableTuple<Board>> getTuplesForCell(int x,int y) {
        List<FlattenSolvableTuple<Board>> result = new ArrayList<>();
        FlattenSolvableTuple<Board> sourcefst = getSourceTuple(x,y);
        if (sourcefst != null) result.add(sourcefst);

        return result;
    }


    public FlattenSolvableTuple<Board> getSourceTuple(int x, int y) {
        if (!isPossession(x,y)) return null;
        if (getCell(x,y).getPathType() != PathType.INITIAL) return null;
        FlattenSolvableTuple<Board> result = new FlattenSolvableTuple<>();

        for (Jump j : jumps.getCell(x,y)) {
            Board next = new Board(this);
            MyMove pro = new MyMove(j,true);
            MyMove anti = new MyMove(j,false);
            pro.applyMove(next);
            result.addTuple(next,anti);
        }

        return result;
    }

}
